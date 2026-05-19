package com.qw.catalog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.constant.CaffeineConstant;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.ServiceCategories;
import com.qw.catalog.mapper.CategoriesMapper;
import com.qw.catalog.service.ICategoriesService;
import com.qw.common.utils.RandomTTL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoriesServiceImpl implements ICategoriesService {

    @Autowired
    CategoriesMapper categoriesMapper;
    @Autowired
    Cache<String, Object> categoriesCache;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ObjectMapper objectMapper;

    @Override
    public List<ServiceCategories> list() throws JsonProcessingException {
        List<ServiceCategories> list = (List<ServiceCategories>) categoriesCache.get(RedisConstant.ALL_CATEGORIES, K -> {
            String json = stringRedisTemplate.opsForValue().get(RedisConstant.ALL_CATEGORIES);
            List<ServiceCategories> temp = null;
            try {
                temp = objectMapper.readValue(json, new TypeReference<List<ServiceCategories>>() {
                });
            } catch (JsonProcessingException e) {
                log.error("{}", e);
                return null;
            }
            return temp;
        });
        if (list == null) {
            log.info("没有命中缓存");
            list = categoriesMapper.list();
            String json = objectMapper.writeValueAsString(list);
            stringRedisTemplate.opsForValue().set(RedisConstant.ALL_CATEGORIES, json, RandomTTL.randomTTL(30));
            categoriesCache.put(RedisConstant.ALL_CATEGORIES, list);
        }
        return list;
    }

    @Override
    public ServiceCategories getById(Long id) throws JsonProcessingException {
        ServiceCategories categories = (ServiceCategories) categoriesCache.get(CaffeineConstant.CATEGORY_PREFIX + id, key -> {
            String json = stringRedisTemplate.opsForValue().get(RedisConstant.CATEGORIES_PREFIX + id);
            if ("".equals(json)) {
                return new ServiceCategories();
            }
            ServiceCategories serviceCategories = null;
            try {
                serviceCategories = objectMapper.readValue(json, ServiceCategories.class);
            } catch (JsonProcessingException e) {
                log.error("{}", e);
                return null;
            }
            return serviceCategories;
        });
        if (categories == null) {
            ServiceCategories sc = categoriesMapper.getById(id);
            if (sc == null) {
                stringRedisTemplate.opsForValue().set(RedisConstant.CATEGORIES_PREFIX + id, "", RandomTTL.randomTTL(1L));
                return null;
            }
            String json = objectMapper.writeValueAsString(sc);
            stringRedisTemplate.opsForValue().set(RedisConstant.CATEGORIES_PREFIX + id, json, RandomTTL.randomTTL(30L));
            categoriesCache.put(CaffeineConstant.CATEGORY_PREFIX + id, sc);
            categories = sc;
        }
        if (categories.getId() == null) {
            return null;
        }
        return categories;
    }
}
