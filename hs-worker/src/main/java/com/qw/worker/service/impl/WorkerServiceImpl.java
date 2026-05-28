package com.qw.worker.service.impl;

import com.qw.common.constant.OrderStatus;
import com.qw.common.constant.RocketMQConstant;
import com.qw.common.entity.Notifications;
import com.qw.common.entity.Workers;
import com.qw.common.exception.BizException;
import com.qw.common.mapper.WorkersMapper;
import com.qw.common.utils.UserContext;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.qw.order.mapper.OrdersMapper;
import com.qw.worker.constant.ErrorConstant;
import com.qw.worker.constant.RedisConstant;
import com.qw.worker.constant.RemarkConstant;
import com.qw.worker.dto.LocationRequest;
import com.qw.worker.service.IWorkerService;
import jakarta.annotation.PostConstruct;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.worker.service.impl
 * @Project：home-serve
 * @name：WorderServiceImpl
 * @Date：2026/5/20 18:45
 * @Filename：WorderServiceImpl
 */
@Service
public class WorkerServiceImpl implements IWorkerService {
    @Autowired
    WorkersMapper workersMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    DefaultRedisScript<Long> script;
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @PostConstruct
    private void init(){
        script=new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/grab_order.lua"));
        script.setResultType(Long.class);
    }

    @Override
    public Workers getProfile() {
        Workers workers = workersMapper.selectById(UserContext.getUserId());
        if (workers == null) {throw new BizException(ErrorConstant.NOT_FIND_ERROR);
        }
        workers.setPasswordHash(null);
            return workers;
        }

    @Override
    public void updateOnlineStatus(Integer status) {
        if(status>1||status<0){throw new BizException(ErrorConstant.STATUS_ERROR);}
        workersMapper.updateOnlineStatus(status,UserContext.getUserId());
    }

    @Override
    public void updateLocation(LocationRequest req) {
        stringRedisTemplate.opsForGeo().add(RedisConstant.WORKER_LOCATION,new Point(req.getLng().doubleValue(),req.getLat().doubleValue()),UserContext.getUserId().toString());
        workersMapper.updateWorkerLocation(UserContext.getUserId(),req.getLng(),req.getLat());
    }

    @Transactional
    @Override
    public Long grabOrder(Long orderId) {
        Long workerId=UserContext.getUserId();
        Long res = stringRedisTemplate.execute(script, Collections.singletonList(RedisConstant.ORDER_GRAB_PREFIX + orderId), workerId.toString());
        if(res==-1){throw new BizException(ErrorConstant.ORDER_NOT_EXISTS);}
        if(res==0){throw new BizException(ErrorConstant.ORDER_HAVE_BE_GRAB);}
        if(res==1){
            Orders orders=Orders.builder().id(orderId).status(OrderStatus.GRABBED.getCode()).workerId(workerId).updatedAt(LocalDateTime.now()).build();
            ordersMapper.updateOrders(orders);

            OrderEvents events=OrderEvents.builder().orderId(orderId).fromStatus(OrderStatus.WAITING.getCode()).toStatus(OrderStatus.GRABBED.getCode()).createdAt(LocalDateTime.now())
                    .operatorType(2).operatorId(workerId).remark(RemarkConstant.ORDER_HAVE_BE_GRAB).build();
            ordersMapper.insertOrderEvent(events);
            stringRedisTemplate.delete(RedisConstant.ORDER_GRAB_PREFIX+orderId);
            Orders grabbed = ordersMapper.getOrderById(orderId);
            Notifications n = Notifications.builder().notificationType(2).createdAt(LocalDateTime.now())
                    .receiverType(0).receiverId(grabbed.getUserId()).relatedOrderId(orderId)
                    .title("服务者已接单").content("您的订单" + grabbed.getOrderNo() + "已被服务者接单").build();
            rocketMQTemplate.syncSend(RocketMQConstant.NOTIFICATION_TOPIC, n);
        }
        return workerId;
    }

    @Override
    public List<Orders> getMyOrder() {
        return ordersMapper.getOrdersByWorkerId(UserContext.getUserId());
    }

    @Transactional
    @Override
    public void startService(Long id) {
        Orders order = ordersMapper.getOrderById(id);
        if(!order.getWorkerId().equals(UserContext.getUserId())){
            throw new BizException(ErrorConstant.AUTH_NOT_ENOUGH);
        }
        if(order.getStatus()==null||order.getStatus()!=OrderStatus.GRABBED.getCode()){throw new BizException(ErrorConstant.STATUS_ERROR);}
        Orders orders=Orders.builder().id(order.getId()).status(OrderStatus.SERVING.getCode()).updatedAt(LocalDateTime.now()).build();
        ordersMapper.updateOrders(orders);

        OrderEvents events=OrderEvents.builder().orderId(id).operatorType(2).operatorId(UserContext.getUserId()).createdAt(LocalDateTime.now())
                .fromStatus(OrderStatus.GRABBED.getCode()).toStatus(OrderStatus.SERVING.getCode()).remark(RemarkConstant.ORDER_START).build();
        ordersMapper.insertOrderEvent(events);

    }

    @Transactional
    @Override
    public void completeService(Long id) {
        Orders order = ordersMapper.getOrderById(id);
        if(!order.getWorkerId().equals(UserContext.getUserId())){
            throw new BizException(ErrorConstant.AUTH_NOT_ENOUGH);
        }
        if(order.getStatus()==null||order.getStatus()!=OrderStatus.SERVING.getCode()){throw new BizException(ErrorConstant.STATUS_ERROR);}
        Orders orders=Orders.builder().id(order.getId()).status(OrderStatus.TO_CONFIRM.getCode()).updatedAt(LocalDateTime.now()).build();
        ordersMapper.updateOrders(orders);

        OrderEvents events=OrderEvents.builder().orderId(id).operatorType(2).operatorId(UserContext.getUserId()).createdAt(LocalDateTime.now())
                .fromStatus(OrderStatus.SERVING.getCode()).toStatus(OrderStatus.TO_CONFIRM.getCode()).remark(RemarkConstant.ORDER_COMPLETED).build();
        ordersMapper.insertOrderEvent(events);

        Notifications n = Notifications.builder().notificationType(2).createdAt(LocalDateTime.now())
                .receiverType(0).receiverId(order.getUserId()).relatedOrderId(id)
                .title("服务已完成").content("您的订单" + order.getOrderNo() + "服务已完成，请确认验收").build();
        rocketMQTemplate.syncSend(RocketMQConstant.NOTIFICATION_TOPIC, n);
    }


}

