package com.qw.order.service;

import com.qw.common.result.Result;
import com.qw.order.dto.CreateOrderRequest;
import com.qw.order.dto.OrderDetailResponse;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 订单主表 服务类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
public interface IOrdersService {

   void createOrder(CreateOrderRequest createOrderRequest,Long userid);

    List<Orders> getMyOrders(Integer status,Long userId,Integer page,Integer size);

    OrderDetailResponse getOrderDetail(Long id);

    List<OrderEvents> getOrderEvent(Long id);

    void payOrders(Long id);

    void cancelOrder(Long id);

 void confirmOrder(Long id);
}
