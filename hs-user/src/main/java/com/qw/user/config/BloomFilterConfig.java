package com.qw.user.config;

import com.qw.catalog.mapper.CategoriesMapper;
import com.qw.catalog.mapper.SkuMapper;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.user.config
 * @Project：home-serve
 * @name：BloomFilterConfig
 * @Date：2026/5/28 17:12
 * @Filename：BloomFilterConfig
 */
@Configuration
public class BloomFilterConfig {

    private static final String SKU_BLOOM_KEY = "bloom:sku";
    private static final String CATEGORY_BLOOM_KEY = "bloom:category";

    @Bean
    public RBloomFilter<Long> skuBloomFilter(RedissonClient redissonClient, SkuMapper skuMapper){
        RBloomFilter<Long> filter=redissonClient.getBloomFilter(SKU_BLOOM_KEY);
        filter.tryInit(10000,0.01);
        if (!filter.contains(-1L)) {
            List<Long> ids = skuMapper.getAllIds();
            ids.forEach(filter::add);
            filter.add(-1L);
        }
        return filter;
    }

    @Bean
    public RBloomFilter<Long> categoryBloomFilter(RedissonClient redissonClient, CategoriesMapper categoriesMapper){
        RBloomFilter<Long> filter=redissonClient.getBloomFilter(CATEGORY_BLOOM_KEY);
        filter.tryInit(10000,0.01);
        if (!filter.contains(-1L)) {
            List<Long> ids = categoriesMapper.getAllIds();
            ids.forEach(filter::add);
            filter.add(-1L);
        }
        return filter;
    }
}
