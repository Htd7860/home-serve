package com.qw.order.controller;

import com.qw.order.dto.CreateOrderRequest;
import com.qw.order.service.IOrdersService;
import com.qw.common.result.Result;
import com.qw.common.utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;



/**
 * <p>
 * 订单主表 前端控制器
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Controller
@RequestMapping("/orders")
public class OrdersController {
    @Autowired
    IOrdersService ordersServiceImpl;
    @PostMapping
    public Result createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest){
        Long userId= UserContext.getUserId();
        return Result.ok(ordersServiceImpl.createOrder(createOrderRequest,userId));
    }
}
