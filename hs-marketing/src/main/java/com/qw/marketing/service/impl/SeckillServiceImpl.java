package com.qw.marketing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.qw.common.constant.SeckillActivityStatus;
import com.qw.common.exception.BizException;
import com.qw.common.utils.UserContext;
import com.qw.marketing.constant.RedisConstant;
import com.qw.common.constant.RocketMQConstant;
import com.qw.marketing.dto.SeckillGrabMessage;
import com.qw.marketing.entity.SeckillActivities;
import com.qw.marketing.mapper.SeckillMapper;
import com.qw.marketing.service.ISeckillService;
import lombok.extern.slf4j.Slf4j;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    RocketMQTemplate rocketMQTemplate;

    private static final DefaultRedisScript<Long> SCRIPT;
    private final RateLimiter rateLimiter=RateLimiter.create(200);
    static{
        SCRIPT=new DefaultRedisScript<>();
        SCRIPT.setLocation(new ClassPathResource("lua/seckill_grab.lua"));
        SCRIPT.setResultType(Long.class);
    }
    @Override
    public List<SeckillActivities> getSeckillActivities() {
        String json = stringRedisTemplate.opsForValue().get(RedisConstant.SECKILL_ACTIVITIES);
        if(json==null){
            List<SeckillActivities> activities = seckillMapper.getAllSeckillActivities();
            try {
                stringRedisTemplate.opsForValue().set(RedisConstant.SECKILL_ACTIVITIES,new ObjectMapper().writeValueAsString(activities),10, TimeUnit.MINUTES);
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

    @Override
    public Long grabSeckill(Long activityId) {
        if(!rateLimiter.tryAcquire(1,TimeUnit.SECONDS)){
            throw new BizException("系统繁忙，请稍后再试!");
        }

        SeckillActivities activities = seckillMapper.getById(activityId);
        if(activities==null){
            throw new BizException("活动不存在");
        }
        if(activities.getStatus()!=SeckillActivityStatus.IN_PROGRESS.getCode()){
            throw new BizException("活动未开始或者已结束");
        }
        LocalDateTime now=LocalDateTime.now();
        if(now.isBefore(activities.getStartTime())||now.isAfter(activities.getEndTime())){
            throw new BizException("不在活动范围内");
        }
        String stockKey=RedisConstant.SECKILL_STOCK_PREFIX+activityId;
        String userKey=RedisConstant.SECKILL_USERS_PREFIX+activityId;
        Long res = stringRedisTemplate.execute(SCRIPT, List.of(stockKey, userKey), UserContext.getUserId().toString());
        if(res==-1){throw new BizException("活动未预热");}
        if(res==0){throw new BizException("库存不足");}
        if(res==-2){throw new BizException("不能重复抢券");}
        if(res==1){
            SeckillGrabMessage message=SeckillGrabMessage.builder().userId(UserContext.getUserId())
                    .activityId(activityId).templateId(activities.getTemplateId()).build();
            rocketMQTemplate.asyncSend(RocketMQConstant.SECKILL_TOPIC + ":" + RocketMQConstant.SECKILL_GRAB_TAG, message,
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                        }
                        @Override
                        public void onException(Throwable throwable) {
                            log.error("秒杀失败,回滚库存{}",throwable);
                            stringRedisTemplate.opsForValue().increment(stockKey);
                            stringRedisTemplate.opsForSet().remove(userKey,UserContext.getUserId().toString());
                        }
                    });
            return activities.getTemplateId();
        }
        throw new BizException("抢券失败");
    }
}
