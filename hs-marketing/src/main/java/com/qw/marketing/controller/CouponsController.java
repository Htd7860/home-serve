package com.qw.marketing.controller;

import com.qw.common.annotation.RequireRole;
import com.qw.common.result.Result;
import com.qw.common.utils.UserContext;
import com.qw.marketing.entity.UserCoupons;
import com.qw.marketing.service.ICouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.marketing.controller
 * @Project：home-serve
 * @name：CouponsController
 * @Date：2026/5/26
 * @Filename：CouponsController
 */

@RestController
@RequestMapping("/coupons")
@Tag(name = "优惠券操作")
public class CouponsController {
    @Autowired
    ICouponService couponServiceImpl;

    @RequireRole({"1"})
    @GetMapping("/available")
    @Operation(summary = "查看可领优惠券")
    public Result getAvailableCoupons() {
        return Result.ok(couponServiceImpl.getAvailableCoupons());
    }

    @RequireRole({"1"})
    @PostMapping("/{templateId}/claim")
    @Operation(summary = "领取优惠券")
    public Result receiveCoupon(@PathVariable Long templateId) {
        couponServiceImpl.receiveCoupon(templateId, UserContext.getUserId());
        return Result.ok();
    }

    @RequireRole({"1"})
    @GetMapping("/my")
    @Operation(summary = "查我的优惠券")
    public Result getMyCoupons(@RequestParam(required = false) Integer status) {
        List<UserCoupons> list = couponServiceImpl.getMyCoupons(UserContext.getUserId(), status);
        return Result.ok(list);
    }
}
