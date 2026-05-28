package com.qw.order.consumer;

import com.qw.common.constant.OrderStatus;
import com.qw.common.constant.PayStatus;
import com.qw.common.constant.RocketMQConstant;
import com.qw.common.dto.OrderTimeOutMessage;
import com.qw.common.exception.BizException;
import com.qw.marketing.mapper.CouponsMapper;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.qw.order.mapper.OrdersMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author：qw
 * @Package：com.qw.order.consumer
 * @Project：home-serve
 * @name：OrderTimeOutConsumer
 * @Date：2026/5/28 14:13
 * @Filename：OrderTimeOutConsumer
 */

@Component
@RocketMQMessageListener(topic = RocketMQConstant.ORDER_TIMEOUT_TOPIC,selectorExpression = RocketMQConstant.ORDER_TIMEOUT_TAG
,consumerGroup = "order-timeout-consumer-group")
public class OrderTimeOutConsumer implements RocketMQListener<OrderTimeOutMessage> {
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    CouponsMapper couponsMapper;

    @Override
    public void onMessage(OrderTimeOutMessage msg) {
        Long id=msg.getId();
        Orders order = ordersMapper.getOrderById(id);
        if(order==null){return;}
        if(order.getStatus().equals(OrderStatus.WAITING.getCode())&&order.getPayStatus().equals(PayStatus.UNPAID.getCode())){
            order.setStatus(OrderStatus.CANCELLED.getCode());
            int rows = ordersMapper.updateOrders(order);if(rows==0){throw new BizException("订单修改失败");
            }

            OrderEvents events=OrderEvents.builder().orderId(id).remark("订单支付超时").createdAt(LocalDateTime.now())
                    .operatorType(3).fromStatus(OrderStatus.WAITING.getCode()).toStatus(OrderStatus.CANCELLED.getCode())
                    .build();
            rows = ordersMapper.insertOrderEvent(events);
            if(rows==0){
            throw new BizException("订单插入失败");
            }

            if(msg.getTemplateId()!=null){
                rows = couponsMapper.backCouponsById(msg.getTemplateId());
                if(rows==0){
                    throw new BizException("优惠券返回失败");
                }
            }
        }
    }
}
