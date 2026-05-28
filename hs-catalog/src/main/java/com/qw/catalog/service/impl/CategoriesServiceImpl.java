package com.qw.catalog.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.ServiceCategories;
import com.qw.catalog.mapper.CategoriesMapper;
import com.qw.catalog.service.ICategoriesService;
import com.qw.common.cache.CacheTemplate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@Slf4j
public class CategoriesServiceImpl implements ICategoriesService {

    @Autowired
    CategoriesMapper categoriesMapper;
    @Resource
    Cache<String, Object> categoriesCache;
    @Autowired
    CacheTemplate cacheTemplate;
    @Resource
    RBloomFilter categoryBloomFilter;

    @Override
    public List<ServiceCategories> list()  {
       return cacheTemplate.getList(RedisConstant.ALL_CATEGORIES,null,ServiceCategories.class,k->categoriesMapper.list(),categoriesCache);
    }

    @Override
    public ServiceCategories getById(Long id) {
        if (!categoryBloomFilter.contains(id)) {
            return null;
        }
        return cacheTemplate.get(RedisConstant.CATEGORIES_PREFIX + id,id,ServiceCategories.class,k->categoriesMapper.getById(id),categoriesCache);
    }
}
