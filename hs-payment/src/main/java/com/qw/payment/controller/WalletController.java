package com.qw.payment.controller;

import com.qw.common.result.Result;
import com.qw.common.utils.UserContext;
import com.qw.payment.dto.WithdrawRequest;
import com.qw.payment.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：qw
 * @Package：com.qw.payment.controller
 * @Project：home-serve
 * @name：WalletController
 * @Date：2026/5/25 17:10
 * @Filename：WalletController
 */
@Tag(name="钱包模块")
@RestController
@RequestMapping("/wallet")
public class WalletController {
    @Autowired
    WalletService walletService;
    @GetMapping("/balance")
    @Operation(summary = "查看余额")
    public Result getBalance(){
        return Result.ok(walletService.getByWorkerId(UserContext.getUserId()));
    }

    @GetMapping("/earnings")
    @Operation(summary = "查看流水")
    public Result getEarnings(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int size){
        return Result.ok(walletService.getEarnings(UserContext.getUserId(),page,size));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "提现操作")
    public Result withdraw(@Validated @RequestBody WithdrawRequest request){
        walletService.withdraw(UserContext.getUserId(),request);
        return Result.ok();
    }

    @GetMapping("/withdraws")
    @Operation(summary = "查看提现记录")
    public Result withdraws(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10")int size){
        return Result.ok(walletService.getWithdraws(UserContext.getUserId(),page,size));
    }
}
