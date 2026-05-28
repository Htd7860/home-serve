package com.qw.message.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qw.common.constant.RedisConstant;
import com.qw.common.dto.WsForwardMessage;
import com.qw.common.exception.BizException;
import com.qw.message.constant.RocketMQConstant;
import com.qw.message.dto.OrderBroadcastMessage;
import com.qw.message.handler.OrderPushHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.message.consumer
 * @Project：home-serve
 * @name：OrderBroadcastConsumer
 * @Date：2026/5/25 15:29
 * @Filename：OrderBroadcastConsumer
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = RocketMQConstant.ORDER_TOPIC,selectorExpression = RocketMQConstant.TAG_ORDER_PAID,consumerGroup = RocketMQConstant.GROUP_ORDER_PUSH)
public class OrderBroadcastConsumer implements RocketMQListener<OrderBroadcastMessage> {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OrderPushHandler orderPushHandler;
    @Value("${ws.node.id}")
    private String nodeId;
    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(OrderBroadcastMessage orderBroadcastMessage) {
      log.info("收到订单广播,来自{}", orderBroadcastMessage.getOrderId());

      List<Long> workIds=getNearByWorkers(orderBroadcastMessage.getLng(),orderBroadcastMessage.getLat());
      if(workIds==null){throw new BizException("查询失败");}
        try {
            String json=new ObjectMapper().writeValueAsString(orderBroadcastMessage);

            for (Long workId : workIds) {
                String targetNode = stringRedisTemplate.opsForValue().get("ws:node:worker:"+workId);
                if(targetNode==null||targetNode.equals(nodeId)){
                    orderPushHandler.pushToWorker(workId,json);
                }else{
                    rocketMQTemplate.syncSend(com.qw.common.constant.RocketMQConstant.WS_FORWARD_TOPIC_PREFIX+targetNode, WsForwardMessage.builder().workerId(workId).type(2).payload(json).build());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("{}",e);
            throw new BizException("json解析失败");
        }
        log.info("推送完毕，目标服务者人数{}",workIds.size());
    }

    private List<Long> getNearByWorkers(BigDecimal lng,BigDecimal lat){
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().radius(RedisConstant.WORKER_LOCATION, new Circle(new Point(lng.doubleValue(), lat.doubleValue()),
                new Distance(5, Metrics.KILOMETERS)), RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance()
                .sortAscending());
        if(results==null){return List.of();}
        return results.getContent().stream().map(c->Long.valueOf(c.getContent().getName())).toList();
    }
}
