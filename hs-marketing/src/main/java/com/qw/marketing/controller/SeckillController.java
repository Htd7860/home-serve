package com.qw.marketing.controller;

import com.qw.common.result.Result;
import com.qw.common.utils.UserContext;
import com.qw.marketing.constant.RedisConstant;
import com.qw.marketing.service.ISeckillService;
import com.qw.user.annotation.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：qw
 * @Package：com.qw.marketing.controller
 * @Project：home-serve
 * @name：SeckillController
 * @Date：2026/5/26 14:41
 * @Filename：SeckilController
 */

@RestController
@RequestMapping("/seckill")
@Tag(name = "秒杀操作")
public class SeckillController {
    @Autowired
    ISeckillService seckillServiceImpl;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @RequireRole({"1"})
    @GetMapping
    @Operation(summary = "查看秒杀活动")
    public Result getSeckillActivities(){
       return Result.ok(seckillServiceImpl.getSeckillActivities());
    }

    @RequireRole({"1"})
    @PostMapping("/{id}/grab")
    @Operation(summary = "秒杀抢券")
    public Result grabSeckillCoupons(@PathVariable Long id){
        Long tempId = seckillServiceImpl.grabSeckill(id);
        return Result.ok(tempId);
    }

    @RequireRole({"1"})
    @GetMapping("/{id}/result")
    @Operation(summary = "查看抢券结果")
    public Result getSeckillResult(@PathVariable Long id){
        Boolean flag = stringRedisTemplate.opsForSet().isMember(RedisConstant.SECKILL_USERS_PREFIX + id, UserContext.getUserId().toString());
        return Result.ok(flag);
    }
}
