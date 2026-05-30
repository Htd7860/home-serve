package com.qw.user.integration;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.mapper.SkuMapper;
import com.qw.catalog.service.ISkuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 集成测试：真实 Spring 上下文 + H2 内存数据库
 * 仅 mock 外部中间件（Redis、RocketMQ、Redisson）
 * Service → Mapper → DB 链路完整走通
 */
@SpringBootTest
@ActiveProfiles("test")
class SkuServiceIntegrationTest {

    // ---- Mock 外部中间件 ----
    @MockBean StringRedisTemplate stringRedisTemplate;
    @MockBean RedissonClient redissonClient;
    @MockBean(name = "skuBloomFilter")
    RBloomFilter<Object> skuBloomFilter;
    @MockBean(name = "categoryBloomFilter")
    RBloomFilter<Object> categoryBloomFilter;
    @MockBean RocketMQTemplate rocketMQTemplate;

    // ---- 真实 bean ----
    @Autowired ISkuService skuService;
    @Autowired SkuMapper skuMapper;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired @Qualifier("skuCache") Cache<String, Object> skuCache;
    @Autowired @Qualifier("pricingCache") Cache<String, Object> pricingCache;

    @BeforeEach
    void setUp() {
        // 清 Caffeine 缓存，避免跨测试数据污染
        skuCache.invalidateAll();
        pricingCache.invalidateAll();

        // 清数据库
        jdbcTemplate.update("DELETE FROM service_skus");
        jdbcTemplate.update("DELETE FROM pricing_rules");

        // Redis 模拟"全量缓存未命中"，请求穿透到 DB
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = org.mockito.Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(ops);

        // 默认布隆过滤器未命中
        when(skuBloomFilter.contains(anyLong())).thenReturn(false);
    }

    // ==================== getByCategory ====================

    @Test
    void getByCategory_ReturnsList() {
        jdbcTemplate.update(
                "INSERT INTO service_skus (category_id, name, base_price, status) VALUES (?,?,?,?)",
                1, "保洁-2小时", new BigDecimal("99"), 1);
        jdbcTemplate.update(
                "INSERT INTO service_skus (category_id, name, base_price, status) VALUES (?,?,?,?)",
                1, "保洁-4小时", new BigDecimal("199"), 1);
        jdbcTemplate.update(
                "INSERT INTO service_skus (category_id, name, base_price, status) VALUES (?,?,?,?)",
                2, "维修-水管", new BigDecimal("299"), 1);

        List<ServiceSkus> result = skuService.getByCategory(1);

        assertEquals(2, result.size());
        result.forEach(s -> assertEquals(1, s.getCategoryId()));
    }

    @Test
    void getByCategory_NoData_ReturnsEmptyList() {
        List<ServiceSkus> result = skuService.getByCategory(999);

        assertTrue(result.isEmpty());
    }

    // ==================== getById ====================

    @Test
    void getById_SkuExists_ReturnsFromDb() {
        when(skuBloomFilter.contains(1L)).thenReturn(true);

        jdbcTemplate.update(
                "INSERT INTO service_skus (id, category_id, name, base_price, status) VALUES (?,?,?,?,?)",
                1L, 1, "集成测试-SKU", new BigDecimal("88.88"), 1);

        ServiceSkus result = skuService.getById(1L);

        assertNotNull(result);
        assertEquals("集成测试-SKU", result.getName());
        assertEquals(new BigDecimal("88.88"), result.getBasePrice());
    }

    @Test
    void getById_BloomFilterMiss_ReturnsNull() {
        // 布隆过滤器返回 false → 直接返回 null，不查 DB
        ServiceSkus result = skuService.getById(999L);

        assertNull(result);
    }

    // ==================== calculateMoney ====================

    @Test
    void calculateMoney_NoRulesInDb_ReturnsBasePrice() {
        BigDecimal[] res = skuService.calculateMoney(
                LocalDateTime.now(), new BigDecimal("100"), false, null);

        assertEquals(new BigDecimal("100"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(BigDecimal.ZERO, res[2]);
    }

    @Test
    void calculateMoney_TimeSurcharge_FromDb() {
        jdbcTemplate.update(
                "INSERT INTO pricing_rules (rule_type, rule_config, status) VALUES (?,?,?)",
                "TIME_SURCHARGE",
                "{\"peakHours\":[\"18:00-22:00\"],\"surchargeRate\":0.2}",
                1);

        LocalDateTime peakTime = LocalDateTime.of(2026, 5, 29, 19, 0);
        BigDecimal[] res = skuService.calculateMoney(peakTime, new BigDecimal("100"), false, null);

        assertEquals(new BigDecimal("120.0"), res[0]);
        assertEquals(new BigDecimal("20.0"), res[1]);
        assertEquals(BigDecimal.ZERO, res[2]);
    }

    @Test
    void calculateMoney_DistanceSurcharge_FromDb() {
        jdbcTemplate.update(
                "INSERT INTO pricing_rules (rule_type, rule_config, status) VALUES (?,?,?)",
                "DISTANCE_SURCHARGE",
                "{\"freeKm\":5,\"pricePerKm\":2.0,\"maxFee\":50}",
                1);

        BigDecimal[] res = skuService.calculateMoney(
                LocalDateTime.now(), new BigDecimal("100"), true, 12.0);

        assertEquals(new BigDecimal("114.0"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(new BigDecimal("14.0"), res[2]);
    }

    @Test
    void calculateMoney_DistanceSurcharge_CappedAtMaxFee() {
        jdbcTemplate.update(
                "INSERT INTO pricing_rules (rule_type, rule_config, status) VALUES (?,?,?)",
                "DISTANCE_SURCHARGE",
                "{\"freeKm\":5,\"pricePerKm\":2.0,\"maxFee\":50}",
                1);

        // 距离 100km → (100-5)*2 = 190, 但 maxFee=50, 最多收 50
        BigDecimal[] res = skuService.calculateMoney(
                LocalDateTime.now(), new BigDecimal("100"), true, 100.0);

        assertEquals(new BigDecimal("150.0"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(new BigDecimal("50.0"), res[2]);
    }

    @Test
    void calculateMoney_BothSurcharges_FromDb() {
        jdbcTemplate.update(
                "INSERT INTO pricing_rules (rule_type, rule_config, status) VALUES (?,?,?)",
                "TIME_SURCHARGE",
                "{\"peakHours\":[\"18:00-22:00\"],\"surchargeRate\":0.2}",
                1);
        jdbcTemplate.update(
                "INSERT INTO pricing_rules (rule_type, rule_config, status) VALUES (?,?,?)",
                "DISTANCE_SURCHARGE",
                "{\"freeKm\":5,\"pricePerKm\":2.0,\"maxFee\":50}",
                1);

        // 高峰 19:00 + 距离 12km → 100*1.2 + (12-5)*2 = 120 + 14 = 134
        LocalDateTime peakTime = LocalDateTime.of(2026, 5, 29, 19, 0);
        BigDecimal[] res = skuService.calculateMoney(peakTime, new BigDecimal("100"), true, 12.0);

        assertEquals(new BigDecimal("134.0"), res[0]);
        assertEquals(new BigDecimal("20.0"), res[1]);
        assertEquals(new BigDecimal("14.0"), res[2]);
    }
}
