package com.qw.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.service.ISkuService;
import com.qw.common.constant.CouponStatus;
import com.qw.common.constant.OrderStatus;
import com.qw.common.constant.PayStatus;
import com.qw.common.constant.PaymentRecordStatus;
import com.qw.common.dto.OrderTimeOutMessage;
import com.qw.common.dto.SettleMessage;
import com.qw.common.entity.Notifications;
import com.qw.common.entity.UserAddresses;
import com.qw.common.exception.BizException;
import com.qw.common.service.IAddressService;
import com.qw.common.service.IUserService;
import com.qw.common.utils.OrderNoUtils;
import com.qw.common.utils.UserContext;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;
import com.qw.marketing.mapper.CouponsMapper;
import com.qw.marketing.service.ICouponService;
import com.qw.message.constant.RocketMQConstant;
import com.qw.message.dto.OrderBroadcastMessage;
import com.qw.order.constant.ErrorConstant;
import com.qw.order.constant.OrderConstant;
import com.qw.order.constant.RedisConstant;
import com.qw.order.dto.CreateOrderRequest;
import com.qw.order.dto.OrderDetailResponse;
import com.qw.order.entity.OrderAddressSnapshots;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.qw.order.mapper.OrdersMapper;
import com.qw.order.service.IOrdersService;
import com.qw.payment.entity.PaymentRecords;
import com.qw.payment.mapper.PaymentMapper;
import com.qw.payment.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
@Slf4j
public class OrdersServiceImpl  implements IOrdersService {
    @Autowired
    ISkuService skuServiceImpl;
    @Autowired
    IUserService userServiceImpl;
    @Autowired
    Cache<String,Object> ordersCache;
    @Autowired
    CouponsMapper couponsMapper;
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RocketMQTemplate rocketMQTemplate;
    @Autowired
    WalletService walletService;

    @Transactional
    @Override
    public void  createOrder(CreateOrderRequest req, Long userid) {
        //进行sku以及地址的校验
        ServiceSkus serviceSkus=null;
        serviceSkus=skuServiceImpl.getById(req.getSkuId());
        if(serviceSkus==null){ throw new BizException(ErrorConstant.SERVICE_NOT_FIND);}
        Integer categoryId = serviceSkus.getCategoryId();
        UserAddresses address= userServiceImpl.getAddressById(req.getAddressId());
        if(address==null){throw new BizException(ErrorConstant.ADDRESS_NOT_FIND);}
        //进行预约时间检测
        LocalDateTime limitTime=req.getAppointedTime().minusMinutes(30L);
        if(limitTime.isBefore(LocalDateTime.now())){throw new BizException(ErrorConstant.APPOINT_TIME_ERROR);}

        //计算价格（时段和距离加价以及优惠券条件判断）
        BigDecimal bigDecimal=serviceSkus.getBasePrice();
        BigDecimal[] bigDecimals = skuServiceImpl.calculateMoney(req.getAppointedTime(), bigDecimal, false, null);
        BigDecimal total=bigDecimals[0];
        BigDecimal time=bigDecimals[1];
        BigDecimal dis=BigDecimal.ZERO;
        BigDecimal payInTruly=total;
        if(req.getCouponId()!=null){
            UserCoupons coupons = couponsMapper.getUserCouponsByid(req.getCouponId(), UserContext.getUserId());
            if(coupons==null||coupons.getExpireTime().isBefore(LocalDateTime.now())||coupons.getStatus()!=CouponStatus.UNUSED.getCode()){
                throw new BizException(ErrorConstant.COUPONS_INVALIDED);
            }

            CouponTemplates templates = couponsMapper.getTemplatesById(coupons.getTemplateId());
            if(templates.getCouponType()==1) {
                BigDecimal thresholdAmount = templates.getThresholdAmount();
                if(total.compareTo(thresholdAmount)<0){
                    throw new BizException(ErrorConstant.COUPONS_NOT_ATTACH_THRESHOLD);
                }
                payInTruly=payInTruly.subtract(templates.getDiscountAmount());
            }else{
                payInTruly=payInTruly.multiply(templates.getDiscountRate());
            }

        }
        BigDecimal charge=BigDecimal.ZERO;
        if(req.getIsUrgent()==1){
            charge = total.multiply(BigDecimal.valueOf(0.2));
            payInTruly=payInTruly.add(charge);
            total=total.add(charge);
        }

        String orderNo= OrderNoUtils.generateOrderNo();
        if(req.getUserRemark()==null){req.setUserRemark("");}
        if(req.getIsUrgent()==null){req.setIsUrgent(0);}
        Orders orders=Orders.builder().orderNo(orderNo).addressId(req.getAddressId()).createdAt(LocalDateTime.now())
                .categoryId(categoryId).basePrice(serviceSkus.getBasePrice()).couponDiscount(total.subtract(payInTruly))
                .finalPrice(payInTruly).distanceFee(dis).timeSurcharge(time).payMethod(1).userRemark(req.getUserRemark())
                .skuId(req.getSkuId()).urgentFee(charge).userId(userid).appointmentTime(req.getAppointedTime())
                .payStatus(PayStatus.UNPAID.getCode()).status(OrderStatus.WAITING.getCode()).isUrgent(req.getIsUrgent()).build();

        ordersMapper.insertOrders(orders);
        Long id =orders.getId();
        if(req.getCouponId()!=null){
            couponsMapper.deleteUserCouponsById(req.getCouponId(),id);
        }

        //生成地址快照
        OrderAddressSnapshots snapshots=OrderAddressSnapshots.builder().contactName(address.getContactName())
                .contactPhone(address.getContactPhone()).lat(address.getLat()).lng(address.getLng())
                .fullAddress(address.getProvince()+address.getCity()+address.getDistrict()+address.getDetail())
                .orderId(id).build();
        ordersMapper.insertAddressSnapshots(snapshots);
        //记录状态变更
        OrderEvents orderEvents=OrderEvents.builder().orderId(id).createdAt(LocalDateTime.now()).fromStatus(-1)
                .toStatus(OrderStatus.WAITING.getCode()).remark("").operatorType(3).build();
            ordersMapper.insertOrderEvent(orderEvents);

            rocketMQTemplate.syncSend(com.qw.common.constant.RocketMQConstant.ORDER_TIMEOUT_TOPIC+":"+ com.qw.common.constant.RocketMQConstant.ORDER_TIMEOUT_TAG,
                    MessageBuilder.withPayload(OrderTimeOutMessage.builder().id(id).templateId(req.getCouponId())).build(),2000,16);

    }

    @Override
    public List<Orders> getMyOrders(Integer status, Long userId, Integer page, Integer size) {
        Page<Orders> pages=new Page<>(page,size);
        return ordersMapper.getMyOrders(status,userId,pages);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long id) {
        Orders order = ordersMapper.getOrderById(id);
        if(order==null||!order.getUserId().equals(UserContext.getUserId())){ throw new BizException(ErrorConstant.ORDER_AUTH_ERROR);}
        List<OrderEvents> events = ordersMapper.getOrderEventByOrderId(id);
        return OrderDetailResponse.builder().orders(order)
                .orderEvents(events).build();
    }

    @Override
    public List<OrderEvents> getOrderEvent(Long id) {
        Orders order = ordersMapper.getOrderById(id);
        if(order==null||!order.getUserId().equals(UserContext.getUserId())){ throw new BizException(ErrorConstant.ORDER_AUTH_ERROR);}
        List<OrderEvents> events = ordersMapper.getOrderEventByOrderId(id);
        return events;
    }

    @Transactional
    @Override
    public void payOrders(Long id) {
        Orders order = ordersMapper.getOrderById(id);
        if(order==null||!order.getUserId().equals(UserContext.getUserId())){
            throw new BizException(ErrorConstant.ORDER_AUTH_ERROR);
        }

        if(order.getStatus()==null||order.getStatus()!=OrderStatus.WAITING.getCode()||order.getPayStatus()==null||order.getPayStatus()!=PayStatus.UNPAID.getCode()){
            throw new BizException(ErrorConstant.ORDER_STATUS_ERROR);
        }
        //todo 支付模块


        //发送订单消息
        Notifications notifications=Notifications.builder().notificationType(2).createdAt(LocalDateTime.now())
                        .receiverType(0).receiverId(order.getUserId()).relatedOrderId(id).title("订单支付成功")
                        .content("您已成功完成订单支付,订单号为:"+order.getOrderNo()+"支付金额为"+order.getFinalPrice()).build();
        rocketMQTemplate.syncSend(com.qw.common.constant.RocketMQConstant.NOTIFICATION_TOPIC,notifications);


        order.setPayStatus(PayStatus.PAID.getCode());order.setPayTime(LocalDateTime.now());
        ordersMapper.updateOrders(order);
        PaymentRecords paymentRecords=PaymentRecords.builder().paymentNo("PAY:"+OrderNoUtils.generateOrderNo()).orderId(id).method(1)
                .status(PaymentRecordStatus.SUCCESS.getCode()).amount(order.getFinalPrice()).paidAt(LocalDateTime.now()).userId(UserContext.getUserId()).build();
        paymentMapper.insertPaymentRecord(paymentRecords);


        OrderEvents orderEvents=OrderEvents.builder().orderId(id).operatorType(3)
                .fromStatus(OrderStatus.WAITING.getCode()).toStatus(OrderStatus.WAITING.getCode())
                .remark(OrderConstant.ORDER_PAY_SUCCESS).build();
        ordersMapper.insertOrderEvent(orderEvents);

        stringRedisTemplate.opsForValue().set(RedisConstant.ORDER_READY_PREFIX+id,"0");

        OrderAddressSnapshots orderAddressSnapshots=ordersMapper.getOrderAddressSnapshotsByOrderId(id);
        OrderBroadcastMessage message=OrderBroadcastMessage.builder().orderId(id).categoryId(order.getCategoryId())
                .appointmentTime(order.getAppointmentTime()).lat(orderAddressSnapshots.getLat())
                .lng(orderAddressSnapshots.getLng()).finalPrice(order.getFinalPrice())
                .build();

        rocketMQTemplate.syncSend(RocketMQConstant.ORDER_TOPIC+":"+RocketMQConstant.TAG_ORDER_PAID,message);
    }

    @Transactional
    @Override
    public void cancelOrder(Long id) {
        Orders order = ordersMapper.getOrderById(id);
        if(order==null||!order.getUserId().equals(UserContext.getUserId())){
            throw new BizException(ErrorConstant.ORDER_AUTH_ERROR);
        }

        if(order.getStatus()==null||!(order.getStatus()==OrderStatus.WAITING.getCode())){throw new BizException(ErrorConstant.ORDER_STATUS_ERROR);}
        if(order.getPayStatus()!=null&&order.getPayStatus()==PayStatus.PAID.getCode()){
            //todo 退款逻辑
           order.setPayStatus(PayStatus.REFUNDED.getCode());
        }
        order.setStatus(OrderStatus.CANCELLED.getCode());order.setUpdatedAt(LocalDateTime.now());
        ordersMapper.updateOrders(order);

        OrderEvents orderEvents=OrderEvents.builder().orderId(id).operatorType(1).operatorId(UserContext.getUserId())
                .fromStatus(OrderStatus.WAITING.getCode()).toStatus(OrderStatus.CANCELLED.getCode())
                .remark(OrderConstant.ORDER_CANCEL_SUCCESS).build();
        ordersMapper.insertOrderEvent(orderEvents);
    }

    @Override
    public void confirmOrder(Long id) {
        Orders order = ordersMapper.getOrderById(id);
        if(order==null||!order.getUserId().equals(UserContext.getUserId())){
            throw new BizException(ErrorConstant.ORDER_AUTH_ERROR);
        }
        if(order.getStatus()==null||order.getStatus()!=OrderStatus.TO_CONFIRM.getCode()){
            throw new BizException(ErrorConstant.ORDER_STATUS_ERROR);
        }

//        //分账操作
//        walletService.settle(order.getWorkerId(),id,order.getFinalPrice());
        SettleMessage settleMessage=SettleMessage.builder().userId(UserContext.getUserId()).orderId(id).finalPrice(order.getFinalPrice())
                .workerId(order.getWorkerId()).build();
        String json=null;
        try {
           json=new ObjectMapper().writeValueAsString(settleMessage);
        } catch (JsonProcessingException e) {
            log.error("json序列化失败");
           throw new BizException("系统异常");
        }
        Message<String> message=MessageBuilder.withPayload(json).build();
        rocketMQTemplate.sendMessageInTransaction("settle-topic:settle-tag",message,null);

    }

    @Transactional
    @Override
    public void refund(Long userId, Long id) {
        Orders order=ordersMapper.getOrderById(id);
        if(order==null||!(order.getUserId().equals(userId))){throw new BizException("没有操作权限");}
        if(order.getStatus()==OrderStatus.WAITING.getCode()&&order.getPayStatus()==PayStatus.PAID.getCode()){
            Orders newOrder=Orders.builder().status(OrderStatus.REFUNDED.getCode()).id(id)
                    .payStatus(PayStatus.REFUNDED.getCode()).updatedAt(LocalDateTime.now()).build();
            ordersMapper.updateOrders(newOrder);
            OrderEvents events=OrderEvents.builder().orderId(id).operatorType(1).operatorId(userId).remark("用户退款")
                    .fromStatus(order.getStatus()).toStatus(OrderStatus.REFUNDED.getCode()).createdAt(LocalDateTime.now()).build();
            ordersMapper.insertOrderEvent(events);
            return;
        }
        throw new BizException("商家已接单，无法退款");
    }


}
