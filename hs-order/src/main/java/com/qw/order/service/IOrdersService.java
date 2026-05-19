package com.qw.order.service;

import com.qw.order.dto.CreateOrderRequest;
import com.qw.order.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.xml.transform.Result;

/**
 * <p>
 * 订单主表 服务类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
public interface IOrdersService {

   Orders createOrder(CreateOrderRequest createOrderRequest,Long userid);
}
