package com.qw.payment.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qw.common.constant.RocketMQConstant;
import com.qw.common.dto.SettleMessage;
import com.qw.payment.mapper.PaymentMapper;
import com.qw.payment.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author：qw
 * @Package：com.qw.payment.consumer
 * @Project：home-serve
 * @name：SettleConsumer
 * @Date：2026/5/28 13:22
 * @Filename：SettleConsumer
 */
@Slf4j
@Component
@RocketMQMessageListener(topic=RocketMQConstant.SETTLE_TOPIC,selectorExpression = RocketMQConstant.SETTLE_TAG,consumerGroup = "settle-consumer-group")
public class SettleConsumer implements RocketMQListener<String> {

    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    WalletService walletService;
    @Override
    public void onMessage(String msg) {
        try {
            SettleMessage message=new ObjectMapper().readValue(msg,SettleMessage.class);
            Long orderId = message.getOrderId();
            int rows = paymentMapper.countEarningByOrderId(orderId);
            if(rows>0){return;}
            walletService.settle(message.getWorkerId(),orderId,message.getFinalPrice());
        } catch (JsonProcessingException e) {
            log.error("{}",e);
        }

    }
}
