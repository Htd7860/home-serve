package com.qw.marketing.controller;

import com.qw.marketing.service.ICouponService;
import com.qw.common.cache.CacheTemplate;
import com.qw.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @Author：qw
 * @Package：com.qw.marketing.controller
 * @Project：home-serve
 * @name：CouponsController
 * @Date：2026/5/19 10:53
 * @Filename：CouponsController
 */
@Tag(name = "优惠券操作")
@RestController
@RequestMapping("/coupons")
public class CouponsController {
    @Autowired
    CacheTemplate cacheTemplate;
    @Autowired
    ICouponService couponServiceImpl;
    @Operation(summary = "查看优惠券（可以设置条件）")
    @GetMapping("/me")
    public Result getMyCoupons(@RequestParam(required = false) Integer status){
        return Result.ok(couponServiceImpl.getMyCoupons(status));
    }

    @Operation(summary = "查看可以领取的优惠券")
    @GetMapping("/available")
    public Result getAvailableCoupons(){

        return Result.ok(couponServiceImpl.getAvailableCoupons());
    }

    @Operation(summary = "领取优惠券" )
    @PostMapping("/{templateId}/claim")
    public Result receiveCoupons(@PathVariable Long templateId){
        return couponServiceImpl.receiveCoupons(templateId);
    }
}
