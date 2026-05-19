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
                .maximumSize(200)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

}
