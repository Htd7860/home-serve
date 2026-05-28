package com.qw.message.consumer;

import com.qw.common.constant.RocketMQConstant;
import com.qw.common.dto.WsForwardMessage;
import com.qw.common.entity.Notifications;
import com.qw.message.handler.NotificationPushHandler;
import com.qw.message.handler.OrderPushHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author：qw
 * @Package：com.qw.message.consumer
 * @Project：home-serve
 * @name：WsForwordConsumer
 * @Date：2026/5/28 9:47
 * @Filename：WsForwardConsumer
 */
@Component
@RocketMQMessageListener(topic = RocketMQConstant.WS_FORWARD_TOPIC_PREFIX+"${ws.node.id}",consumerGroup = "ws-forward-consumer-group")
public class WsForwardConsumer implements RocketMQListener<WsForwardMessage> {
    @Autowired
    NotificationPushHandler notificationPushHandler;
    @Autowired
    OrderPushHandler orderPushHandler;

    @Override
    public void onMessage(WsForwardMessage wsForwardMessage) {
        Integer type = wsForwardMessage.getType();
        if(type==1){
            Notifications notification = wsForwardMessage.getNotification();
            notificationPushHandler.send(notification);
        }else if(type==2){
            String payload = wsForwardMessage.getPayload();
            orderPushHandler.pushToWorker(wsForwardMessage.getWorkerId(),payload);
        }

    }
}
