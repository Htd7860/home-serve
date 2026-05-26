package com.qw.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.service.ISkuService;
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
        try {
            serviceSkus=skuServiceImpl.getById(req.getSkuId());
        } catch (JsonProcessingException e) {
            log.error("{}",e);

        }
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
            if(coupons==null||coupons.getExpireTime().isBefore(LocalDateTime.now())||coupons.getStatus()!=0){
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
                .skuId(req.getSkuId()).urgentFee(charge).userId(userid).appointmentTime(req.getAppointedTime()).payStatus(0).status(0).isUrgent(req.getIsUrgent()).build();

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
        OrderEvents orderEvents=OrderEvents.builder().orderId(id).createdAt(LocalDateTime.now()).fromStatus(-1).toStatus(1)
                .remark("").operatorType(3).build();
            ordersMapper.insertOrderEvent(orderEvents);
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

        if(order.getStatus()==null||order.getStatus()!=0||order.getPayStatus()==null||order.getPayStatus()!=0){
            throw new BizException(ErrorConstant.ORDER_STATUS_ERROR);
        }
        //todo 支付模块

        order.setPayStatus(1);order.setPayTime(LocalDateTime.now());
        ordersMapper.updateOrders(order);
        PaymentRecords paymentRecords=PaymentRecords.builder().paymentNo("PAY:"+OrderNoUtils.generateOrderNo()).orderId(id).method(1)
                .status(1).amount(order.getFinalPrice()).paidAt(LocalDateTime.now()).userId(UserContext.getUserId()).build();
        paymentMapper.insertPaymentRecord(paymentRecords);


        OrderEvents orderEvents=OrderEvents.builder().orderId(id).operatorType(3).fromStatus(0).toStatus(0)
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

        if(order.getStatus()==null||!(order.getStatus()==0)){throw new BizException(ErrorConstant.ORDER_STATUS_ERROR);}
        if(order.getPayStatus()!=null&&order.getPayStatus()==1){
            //todo 退款逻辑
           order.setPayStatus(3);
        }
        order.setStatus(5);order.setUpdatedAt(LocalDateTime.now());
        ordersMapper.updateOrders(order);

        OrderEvents orderEvents=OrderEvents.builder().orderId(id).operatorType(1).operatorId(UserContext.getUserId()).fromStatus(0).toStatus(5)
                .remark(OrderConstant.ORDER_CANCEL_SUCCESS).build();
        ordersMapper.insertOrderEvent(orderEvents);
    }

    @Transactional
    @Override
    public void confirmOrder(Long id) {
        Orders order = ordersMapper.getOrderById(id);
        if(order==null||!order.getUserId().equals(UserContext.getUserId())){
            throw new BizException(ErrorConstant.ORDER_AUTH_ERROR);
        }
        if(order.getStatus()==null||order.getStatus()!=3){
            throw new BizException(ErrorConstant.ORDER_STATUS_ERROR);
        }

        order.setUpdatedAt(LocalDateTime.now());order.setConfirmTime(LocalDateTime.now());
        order.setStatus(4);
        ordersMapper.updateOrders(order);

        OrderEvents orderEvents=OrderEvents.builder().orderId(id).operatorType(1).operatorId(UserContext.getUserId()).fromStatus(3).toStatus(4)
                .remark(OrderConstant.ORDER_CONFIRM_SUCCESS).build();
        ordersMapper.insertOrderEvent(orderEvents);

        walletService.settle(order.getWorkerId(),id,order.getFinalPrice());
    }

    @Transactional
    @Override
    public void refund(Long userId, Long id) {
        Orders order=ordersMapper.getOrderById(id);
        if(order==null||!(order.getUserId().equals(userId))){throw new BizException("没有操作权限");}
        if(order.getStatus()==0&&order.getPayStatus()==1){
            Orders newOrder=Orders.builder().status(7).id(id).payStatus(4).updatedAt(LocalDateTime.now()).build();
            ordersMapper.updateOrders(newOrder);
            OrderEvents events=OrderEvents.builder().orderId(id).operatorType(1).operatorId(userId).remark("用户退款")
                    .fromStatus(order.getStatus()).toStatus(7).createdAt(LocalDateTime.now()).build();
            ordersMapper.insertOrderEvent(events);
            return;
        }
        throw new BizException("商家已接单，无法退款");
    }


}
