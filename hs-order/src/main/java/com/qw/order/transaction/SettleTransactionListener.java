package com.qw.order.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qw.common.constant.OrderStatus;
import com.qw.common.dto.SettleMessage;
import com.qw.common.entity.Notifications;
import com.qw.order.constant.OrderConstant;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.qw.order.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Author：qw
 * @Package：com.qw.order.transaction
 * @Project：home-serve
 * @name：SettleTransactionListener
 * @Date：2026/5/28 12:18
 * @Filename：SettleTransactionListener
 */
@Slf4j
@Component
@RocketMQTransactionListener
public class SettleTransactionListener implements RocketMQLocalTransactionListener {
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    RocketMQTemplate rocketMQTemplate;
    @Autowired
    @Lazy
    SettleTransactionListener settleTransactionListener;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        String json = (String) message.getPayload();
        try {
            SettleMessage msg=new ObjectMapper().readValue(json,SettleMessage.class);
            Orders order = ordersMapper.getOrderById(msg.getOrderId());
            if(order==null||order.getStatus()!= OrderStatus.TO_CONFIRM.getCode()){log.error("订单状态出错了");return RocketMQLocalTransactionState.ROLLBACK;}
            RocketMQLocalTransactionState state = settleTransactionListener.updateDB(order, msg);
            if(state.equals(RocketMQLocalTransactionState.ROLLBACK)){return RocketMQLocalTransactionState.ROLLBACK;}
            //发送通知
        Notifications n = Notifications.builder().notificationType(2).createdAt(LocalDateTime.now())
                .receiverType(1).receiverId(order.getWorkerId()).relatedOrderId(msg.getOrderId())
                .title("订单已验收").content("订单" + order.getOrderNo() + "已被用户确认验收").build();
        rocketMQTemplate.syncSend(com.qw.common.constant.RocketMQConstant.NOTIFICATION_TOPIC, n);
        } catch (JsonProcessingException e) {
            log.error("{}",e);
         return RocketMQLocalTransactionState.ROLLBACK;
        }

        return RocketMQLocalTransactionState.COMMIT;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {

        String json=(String) message.getPayload();
        try {
            SettleMessage settleMessage = new ObjectMapper().readValue(json, SettleMessage.class);
            Orders order = ordersMapper.getOrderById(settleMessage.getOrderId());
            if (order == null || order.getStatus() != OrderStatus.COMPLETED.getCode()) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (JsonProcessingException e) {
            log.error("{}",e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        return RocketMQLocalTransactionState.COMMIT;
    }

    @Transactional
    public RocketMQLocalTransactionState updateDB(Orders order,SettleMessage msg){
        //        //修改订单状态
        order.setUpdatedAt(LocalDateTime.now());order.setConfirmTime(LocalDateTime.now());
        order.setStatus(OrderStatus.COMPLETED.getCode());
        int rows = ordersMapper.updateOrders(order);
        if(rows==0){return RocketMQLocalTransactionState.ROLLBACK;}
        //        //添加订单事务
        OrderEvents orderEvents=OrderEvents.builder().orderId(msg.getOrderId()).operatorType(1).operatorId(msg.getUserId())
                .fromStatus(OrderStatus.TO_CONFIRM.getCode()).toStatus(OrderStatus.COMPLETED.getCode())
                .remark(OrderConstant.ORDER_CONFIRM_SUCCESS).build();
        rows=ordersMapper.insertOrderEvent(orderEvents);
        if(rows==0){return RocketMQLocalTransactionState.ROLLBACK;}
        return RocketMQLocalTransactionState.COMMIT;
    }

}
