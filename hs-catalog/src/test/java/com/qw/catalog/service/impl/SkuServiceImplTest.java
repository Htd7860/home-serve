package com.qw.catalog.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.PricingRules;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.mapper.PricingRuleMapper;
import com.qw.catalog.mapper.SkuMapper;
import com.qw.common.cache.CacheTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkuServiceImplTest {

    @Mock CacheTemplate cacheTemplate;
    @Mock SkuMapper skuMapper;
    @Mock PricingRuleMapper pricingRuleMapper;
    @Mock Cache<String, Object> skuCache;
    @Mock Cache<String, Object> pricingCache;
    @Mock RBloomFilter<Object> skuBloomFilter;

    @InjectMocks
    SkuServiceImpl skuService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(skuService, "skuBloomFilter", skuBloomFilter);
    }

    // ==================== getByCategory ====================

    @Test
    void getByCategory_ReturnsList() {
        List<ServiceSkus> expected = List.of(new ServiceSkus());
        doReturn(expected).when(cacheTemplate)
                .getList(eq(RedisConstant.SKUS_PREFIX + 1), eq(1), eq(ServiceSkus.class), any(), eq(skuCache));

        List<ServiceSkus> result = skuService.getByCategory(1);

        assertEquals(expected, result);
        verify(cacheTemplate).getList(
                eq(RedisConstant.SKUS_PREFIX + 1), eq(1), eq(ServiceSkus.class), any(), eq(skuCache));
    }

    // ==================== getById ====================

    @Test
    void getById_BloomFilterNotExist_ReturnsNull() {
        when(skuBloomFilter.contains(99L)).thenReturn(false);

        ServiceSkus result = skuService.getById(99L);

        assertNull(result);
        verify(cacheTemplate, never()).get(anyString(), any(), any(), any(), any());
    }

    @Test
    void getById_Exists_ReturnsData() {
        ServiceSkus expected = new ServiceSkus();
        expected.setId(1L);
        when(skuBloomFilter.contains(1L)).thenReturn(true);
        doReturn(expected).when(cacheTemplate)
                .get(eq(RedisConstant.SKUS_SINGLE_PREFIX + 1L), eq(1L), eq(ServiceSkus.class), any(), eq(skuCache));

        ServiceSkus result = skuService.getById(1L);

        assertEquals(expected, result);
        verify(cacheTemplate).get(
                eq(RedisConstant.SKUS_SINGLE_PREFIX + 1L), eq(1L), eq(ServiceSkus.class), any(), eq(skuCache));
    }

    // ==================== calculateMoney ====================

    @Test
    void calculateMoney_NullRules_ReturnsBasePriceOnly() {
        doReturn(null).when(cacheTemplate)
                .getList(eq(RedisConstant.PRICING_RULE_KEY), isNull(), eq(PricingRules.class), any(), eq(pricingCache));

        BigDecimal[] res = skuService.calculateMoney(LocalDateTime.now(), new BigDecimal("100"), false, 12.0);

        assertEquals(new BigDecimal("100"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(BigDecimal.ZERO, res[2]);
    }

    @Test
    void calculateMoney_EmptyRules_ReturnsBasePriceOnly() {
        doReturn(List.of()).when(cacheTemplate)
                .getList(eq(RedisConstant.PRICING_RULE_KEY), isNull(), eq(PricingRules.class), any(), eq(pricingCache));

        BigDecimal[] res = skuService.calculateMoney(LocalDateTime.now(), new BigDecimal("100"), false, 12.0);

        assertEquals(new BigDecimal("100"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(BigDecimal.ZERO, res[2]);
    }

    // ========== 高峰期加价 ==========

    @Test
    void calculateMoney_TimeSurcharge_InPeakHours() {
        LocalDateTime peakTime = LocalDateTime.of(2026, 5, 29, 19, 0);
        PricingRules rule = rule("TIME_SURCHARGE",
                "{\"peakHours\":[\"18:00-22:00\"],\"surchargeRate\":0.2}");
        doReturn(List.of(rule)).when(cacheTemplate)
                .getList(eq(RedisConstant.PRICING_RULE_KEY), isNull(), eq(PricingRules.class), any(), eq(pricingCache));

        BigDecimal[] res = skuService.calculateMoney(peakTime, new BigDecimal("100"), false, 0.0);

        assertEquals(new BigDecimal("120.0"), res[0]);
        assertEquals(new BigDecimal("20.0"), res[1]);
        assertEquals(BigDecimal.ZERO, res[2]);
    }

    @Test
    void calculateMoney_TimeSurcharge_NotInPeakHours() {
        LocalDateTime offPeak = LocalDateTime.of(2026, 5, 29, 14, 0);
        PricingRules rule = rule("TIME_SURCHARGE",
                "{\"peakHours\":[\"18:00-22:00\"],\"surchargeRate\":0.2}");
        doReturn(List.of(rule)).when(cacheTemplate)
                .getList(eq(RedisConstant.PRICING_RULE_KEY), isNull(), eq(PricingRules.class), any(), eq(pricingCache));

        BigDecimal[] res = skuService.calculateMoney(offPeak, new BigDecimal("100"), false, 0.0);

        assertEquals(new BigDecimal("100"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(BigDecimal.ZERO, res[2]);
    }

    // ========== 距离加价 ==========

    @Test
    void calculateMoney_DistanceSurcharge_WithDistance() {
        PricingRules rule = rule("DISTANCE_SURCHARGE",
                "{\"freeKm\":5,\"pricePerKm\":2.0,\"maxFee\":50}");
        doReturn(List.of(rule)).when(cacheTemplate)
                .getList(eq(RedisConstant.PRICING_RULE_KEY), isNull(), eq(PricingRules.class), any(), eq(pricingCache));

        BigDecimal[] res = skuService.calculateMoney(LocalDateTime.now(), new BigDecimal("100"), true, 12.0);

        assertEquals(new BigDecimal("114.0"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(new BigDecimal("14.0"), res[2]);
    }

    @Test
    void calculateMoney_DistanceSurcharge_NoDistance() {
        PricingRules rule = rule("DISTANCE_SURCHARGE",
                "{\"freeKm\":5,\"pricePerKm\":2.0,\"maxFee\":50}");
        doReturn(List.of(rule)).when(cacheTemplate)
                .getList(eq(RedisConstant.PRICING_RULE_KEY), isNull(), eq(PricingRules.class), any(), eq(pricingCache));

        BigDecimal[] res = skuService.calculateMoney(LocalDateTime.now(), new BigDecimal("100"), false, 99.0);

        assertEquals(new BigDecimal("100"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(BigDecimal.ZERO, res[2]);
    }

    @Test
    void calculateMoney_DistanceSurcharge_HitsMaxFee() {
        PricingRules rule = rule("DISTANCE_SURCHARGE",
                "{\"freeKm\":5,\"pricePerKm\":2.0,\"maxFee\":50}");
        doReturn(List.of(rule)).when(cacheTemplate)
                .getList(eq(RedisConstant.PRICING_RULE_KEY), isNull(), eq(PricingRules.class), any(), eq(pricingCache));

        BigDecimal[] res = skuService.calculateMoney(LocalDateTime.now(), new BigDecimal("100"), true, 100.0);

        assertEquals(new BigDecimal("150.0"), res[0]);
        assertEquals(BigDecimal.ZERO, res[1]);
        assertEquals(new BigDecimal("50.0"), res[2]);
    }

    // ========== 两种规则叠加 ==========

    @Test
    void calculateMoney_BothSurcharges() {
        LocalDateTime peakTime = LocalDateTime.of(2026, 5, 29, 19, 0);
        PricingRules timeRule = rule("TIME_SURCHARGE",
                "{\"peakHours\":[\"18:00-22:00\"],\"surchargeRate\":0.2}");
        PricingRules distRule = rule("DISTANCE_SURCHARGE",
                "{\"freeKm\":5,\"pricePerKm\":2.0,\"maxFee\":50}");
        doReturn(List.of(timeRule, distRule)).when(cacheTemplate)
                .getList(eq(RedisConstant.PRICING_RULE_KEY), isNull(), eq(PricingRules.class), any(), eq(pricingCache));

        BigDecimal[] res = skuService.calculateMoney(peakTime, new BigDecimal("100"), true, 12.0);

        assertEquals(new BigDecimal("134.0"), res[0]);
        assertEquals(new BigDecimal("20.0"), res[1]);
        assertEquals(new BigDecimal("14.0"), res[2]);
    }

    // ========== 工具方法 ==========

    private PricingRules rule(String type, String config) {
        PricingRules r = new PricingRules();
        r.setRuleType(type);
        r.setRuleConfig(config);
        return r;
    }
}
