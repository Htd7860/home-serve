package com.qw.message.consumer;

import com.qw.common.constant.RocketMQConstant;
import com.qw.common.entity.Notifications;
import com.qw.common.exception.BizException;
import com.qw.common.mapper.NotificationMapper;
import com.qw.message.handler.NotificationPushHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public void onMessage(Notifications notifications) {
        int rows=notificationMapper.insert(notifications);
        if(rows==0){
            log.error("写入失败:{}",notifications);
            return;
        }
        notificationPushHandler.send(notifications);
    }
}
