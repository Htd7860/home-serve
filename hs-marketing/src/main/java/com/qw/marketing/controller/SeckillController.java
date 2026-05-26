package com.qw.marketing.controller;

import com.qw.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：qw
 * @Package：com.qw.marketing.controller
 * @Project：home-serve
 * @name：SeckilController
 * @Date：2026/5/26 14:41
 * @Filename：SeckilController
 */
@RestController
@RequestMapping("/seckill")
public class SeckillController {
    @GetMapping
    public Result getSeckillActivities(){
       return Result.ok();
    }
}
