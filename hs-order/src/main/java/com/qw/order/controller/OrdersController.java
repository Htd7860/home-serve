package com.qw.order.controller;

import com.qw.common.annotation.RequireRole;
import com.qw.order.dto.CreateOrderRequest;
import com.qw.order.service.IOrdersService;
import com.qw.common.result.Result;
import com.qw.common.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单主表 前端控制器
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */

@RestController
@RequestMapping("/orders")
@Tag(name = "订单操作")
public class OrdersController {
    @Autowired
    IOrdersService ordersServiceImpl;
    @RequireRole({"1"})
    @Operation(summary = "插入订单")
    @PostMapping
    public Result createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest){
        Long userId= UserContext.getUserId();
        ordersServiceImpl.createOrder(createOrderRequest,userId);
        return Result.ok();
    }

    @RequireRole({"1"})
    @Operation(summary = "查看我的所有订单")
    @GetMapping
    public Result getMyOrders(@RequestParam(required = false)Integer status,@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer size){
        return Result.ok(ordersServiceImpl.getMyOrders(status,UserContext.getUserId(),page,size));
    }

    @RequireRole({"1"})
    @Operation(summary = "查看订单详情")
    @GetMapping("/{id}")
    public Result getOrderDetail(@PathVariable Long id){
        return Result.ok(ordersServiceImpl.getOrderDetail(id));
    }

    @RequireRole({"1"})
    @Operation(summary = "返回订单状态变更事件")
    @GetMapping("/{id}/events")
    public Result getOrderEvent(@PathVariable Long id){
        return Result.ok(ordersServiceImpl.getOrderEvent(id));
    }

    @RequireRole({"1"})
    @Operation(summary = "支付订单")
    @PutMapping("/{id}/pay")
    public Result payOrders(@PathVariable Long id){
        ordersServiceImpl.payOrders(id);
        return Result.ok();
    }

    @RequireRole({"1"})
    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result cancelOrder(@PathVariable Long id){
        ordersServiceImpl.cancelOrder(id);
        return Result.ok();
    }

    @RequireRole({"1"})
    @Operation(summary = "验收订单")
    @PutMapping("/{id}/confirm")
    public Result confirmOrder(@PathVariable Long id){
        ordersServiceImpl.confirmOrder(id);
        return Result.ok();
    }

    @RequireRole({"1"})
    @Operation(summary = "申请退款")
    @PostMapping("/{id}/refund")
    public Result refund(@PathVariable Long id){
        ordersServiceImpl.refund(UserContext.getUserId(),id);
        return Result.ok();
    }
}
