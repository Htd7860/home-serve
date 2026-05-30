package com.qw.user.integration;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.entity.ServiceCategories;
import com.qw.catalog.service.ICategoriesService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CategoriesServiceIntegrationTest {

    @MockBean StringRedisTemplate stringRedisTemplate;
    @MockBean RedissonClient redissonClient;
    @MockBean(name = "skuBloomFilter")
    RBloomFilter<Object> skuBloomFilter;
    @MockBean(name = "categoryBloomFilter")
    RBloomFilter<Object> categoryBloomFilter;
    @MockBean RocketMQTemplate rocketMQTemplate;

    @Autowired ICategoriesService categoriesService;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired @Qualifier("categoriesCache") Cache<String, Object> categoriesCache;

    @BeforeEach
    void setUp() {
        categoriesCache.invalidateAll();
        jdbcTemplate.update("DELETE FROM service_categories");

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = org.mockito.Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(ops);
        when(categoryBloomFilter.contains(anyLong())).thenReturn(false);
    }

    // ==================== list ====================

    @Test
    void list_ReturnsAllCategories() {
        jdbcTemplate.update(
                "INSERT INTO service_categories (name, sort_order, status) VALUES (?,?,?)",
                "家政保洁", 1, 1);
        jdbcTemplate.update(
                "INSERT INTO service_categories (name, sort_order, status) VALUES (?,?,?)",
                "搬家拉货", 2, 1);

        List<ServiceCategories> result = categoriesService.list();

        assertEquals(2, result.size());
    }

    @Test
    void list_EmptyTable_ReturnsEmpty() {
        List<ServiceCategories> result = categoriesService.list();

        assertTrue(result.isEmpty());
    }

    // ==================== getById ====================

    @Test
    void getById_BloomFilterMiss_ReturnsNull() {
        ServiceCategories result = categoriesService.getById(99L);

        assertNull(result);
    }

    @Test
    void getById_CategoryExists_ReturnsFromDb() {
        when(categoryBloomFilter.contains(1L)).thenReturn(true);
        jdbcTemplate.update(
                "INSERT INTO service_categories (id, name, sort_order, status) VALUES (?,?,?,?)",
                1, "家政保洁", 1, 1);

        ServiceCategories result = categoriesService.getById(1L);

        assertNotNull(result);
        assertEquals("家政保洁", result.getName());
    }
}
