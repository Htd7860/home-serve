package com.qw.marketing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qw.common.exception.BizException;
import com.qw.marketing.controller.RedisConstant;
import com.qw.marketing.entity.SeckillActivities;
import com.qw.marketing.mapper.SeckillMapper;
import com.qw.marketing.service.ISeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.marketing.service.impl
 * @Project：home-serve
 * @name：SeckillServiceImpl
 * @Date：2026/5/26 14:46
 * @Filename：SeckillServiceImpl
 */
@Slf4j
@Service
public class SeckillServiceImpl implements ISeckillService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    SeckillMapper seckillMapper;
    @Override
    public List<SeckillActivities> getSeckillActivities() {
        String json = stringRedisTemplate.opsForValue().get(RedisConstant.SECKILL_ACTIVITIES);
        if(json==null){
            List<SeckillActivities> activities = seckillMapper.getAllSeckillActivities();
            try {
                stringRedisTemplate.opsForValue().set(RedisConstant.SECKILL_ACTIVITIES,new ObjectMapper().writeValueAsString(activities));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new BizException("缓存写入失败");
            }
            return activities;
        }
        try {
            List<SeckillActivities> activities = new ObjectMapper().readValue(json, new TypeReference<List<SeckillActivities>>() {
            });
            return activities;
        } catch (JsonProcessingException e) {
            log.error("{}",e);
            stringRedisTemplate.delete(RedisConstant.SECKILL_ACTIVITIES);
            throw new BizException("json解析失败");
        }

    }
}
