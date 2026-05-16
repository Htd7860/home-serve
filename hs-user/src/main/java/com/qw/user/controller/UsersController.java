package com.qw.user.controller;

import common.result.Result;
import common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * C端用户 前端控制器
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {
    @GetMapping("/test")
    public Result test(){
        log.info("信息：{}",UserContext.getUserId());
        return Result.ok();
    }
}
