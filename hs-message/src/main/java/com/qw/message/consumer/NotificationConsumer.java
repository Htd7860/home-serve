package com.qw.message.consumer;

import com.qw.common.constant.RocketMQConstant;
import com.qw.common.dto.WsForwardMessage;
import com.qw.common.entity.Notifications;
import com.qw.common.exception.BizException;
import com.qw.common.mapper.NotificationMapper;
import com.qw.message.handler.NotificationPushHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


/**
 * @Author：qw
 * @Package：com.qw.message.consumer
 * @Project：home-serve
 * @name：NotificationConsumer
 * @Date：2026/5/27 13:56
 * @Filename：NotificationConsumer
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQConstant.NOTIFICATION_TOPIC,consumerGroup = "notification-consumer-group")
public class NotificationConsumer implements RocketMQListener<Notifications> {
    @Autowired
    NotificationMapper notificationMapper;
    @Autowired
    NotificationPushHandler notificationPushHandler;
    @Value("${ws.node.id}")
    String nodeId;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Notifications notifications) {
        int i = notificationMapper.countDuplicate(notifications);
        if(i>0){return ;}
        int rows=notificationMapper.insert(notifications);
        if(rows==0){
            log.error("写入失败:{}",notifications);
            return;
        }

        String key=notifications.getReceiverType()+":"+notifications.getReceiverId();
        String target = stringRedisTemplate.opsForValue().get("ws:node:"+key);
        if(target==null||target.equals(nodeId)){
            notificationPushHandler.send(notifications);
        }else{
            rocketMQTemplate.syncSend(RocketMQConstant.WS_FORWARD_TOPIC_PREFIX+target, WsForwardMessage.builder().type(1).notification(notifications).build());
        }

    }
}
