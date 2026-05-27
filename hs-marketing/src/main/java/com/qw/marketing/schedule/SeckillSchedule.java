package com.qw.marketing.schedule;

import com.qw.marketing.constant.RedisConstant;
import com.qw.marketing.entity.SeckillActivities;
import com.qw.marketing.mapper.SeckillMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author：qw
 * @Package：com.qw.marketing.schedule
 * @Project：home-serve
 * @name：SeckillSchedule
 * @Date：2026/5/27 12:01
 * @Filename：SeckillSchedule
 */
@Slf4j
@Component
public class SeckillSchedule {
    @Autowired
    SeckillMapper seckillMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 * * * *")
    public void preheat(){
        List<SeckillActivities> activities = seckillMapper.selectPendingPreheat();
        if(activities!=null&&activities.size()>0){
            Map<String,String> map=new HashMap<>();
            for (SeckillActivities activity : activities) {
                map.put(RedisConstant.SECKILL_STOCK_PREFIX+activity.getId(),activity.getTotalStock().toString());
            }
            stringRedisTemplate.opsForValue().multiSet(map);
            for (SeckillActivities activity : activities) {
              seckillMapper.updateStatus(activity.getId(),1);
            }
            stringRedisTemplate.delete(RedisConstant.SECKILL_ACTIVITIES);
        }
    }

    @Scheduled(cron = "0 * * * *")
    public void start(){
        int rows=seckillMapper.batchStart();
        if(rows>0){
            stringRedisTemplate.delete(RedisConstant.SECKILL_ACTIVITIES);
        }
    }

    @Scheduled(cron = "0 * * * *")
    public void end(){
        int rows=seckillMapper.batchEnd();
        if(rows>0){
            stringRedisTemplate.delete(RedisConstant.SECKILL_ACTIVITIES);
        }
    }
}
