package com.qw.message.consumer;

import com.qw.common.constant.RedisConstant;
import com.qw.marketing.dto.SeckillGrabMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author：qw
 * @Package：com.qw.message.consumer
 * @Project：home-serve
 * @name：SeckillGrabDlqConsumer
 * @Date：2026/5/29 12:00
 * @Filename：SeckillGrabDlqConsumer
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "%DLQ%seckill-grab-consumer-group",consumerGroup = "seckill-grab-dlp-consumer-group")
public class SeckillGrabDlqConsumer implements RocketMQListener<SeckillGrabMessage> {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Override
    public void onMessage(SeckillGrabMessage seckillGrabMessage) {
        Long userId = seckillGrabMessage.getUserId();
        Long activityId = seckillGrabMessage.getActivityId();
        stringRedisTemplate.opsForValue().increment(RedisConstant.SECKILL_STOCK_PREFIX +activityId);
        stringRedisTemplate.opsForSet().remove("seckill:users:"+activityId,userId);

        log.warn("死信队列补偿 活动id={},用户id={}",activityId,userId);
    }
}
