package com.qw.user.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Object> categoriesCache() {
        return Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(250)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public Cache<String, Object> skuCache() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public Cache<String, Object> pricingCache() {
        return Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(200)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

}
