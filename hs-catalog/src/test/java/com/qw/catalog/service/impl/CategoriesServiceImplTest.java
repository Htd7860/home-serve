package com.qw.catalog.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.ServiceCategories;
import com.qw.catalog.mapper.CategoriesMapper;
import com.qw.common.cache.CacheTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriesServiceImplTest {

    @Mock CategoriesMapper categoriesMapper;
    @Mock Cache<String, Object> categoriesCache;
    @Mock CacheTemplate cacheTemplate;
    @Mock RBloomFilter<Object> categoryBloomFilter;

    @InjectMocks
    CategoriesServiceImpl categoriesService;

    // ==================== list ====================

    @BeforeEach
    void init(){
        ReflectionTestUtils.setField(categoriesService,"categoryBloomFilter",categoryBloomFilter);
    }
    @Test
    void list_ReturnsData() {
        List<ServiceCategories> expected = Collections.singletonList(new ServiceCategories());
        // cacheTemplate.getList 返回预期数据（lambda 不会真正执行）
        doReturn(expected).when(cacheTemplate).getList(anyString(), isNull(), any(), any(), any());

        List<ServiceCategories> result = categoriesService.list();

        assertEquals(expected, result);
        verify(cacheTemplate).getList(
                eq(RedisConstant.ALL_CATEGORIES), isNull(), eq(ServiceCategories.class),
                any(), eq(categoriesCache));
    }

    // ==================== getById ====================

    @Test
    void getById_BloomFilterNotExist_ReturnsNull() {
        when(categoryBloomFilter.contains(1L)).thenReturn(false);

        ServiceCategories result = categoriesService.getById(1L);

        assertNull(result);
        verify(cacheTemplate, never()).get(anyString(), any(), any(), any(), any());
    }

    @Test
    void getById_Exists_ReturnsData() {
        ServiceCategories expected = new ServiceCategories();
        expected.setId(1);
        when(categoryBloomFilter.contains(1L)).thenReturn(true);
        doReturn(expected).when(cacheTemplate).get(anyString(), any(), any(), any(), any());

        ServiceCategories result = categoriesService.getById(1L);

        assertEquals(expected, result);
        verify(cacheTemplate).get(
                eq(RedisConstant.CATEGORIES_PREFIX + 1L), eq(1L),
                eq(ServiceCategories.class), any(), eq(categoriesCache));
    }
}
