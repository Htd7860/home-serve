package com.qw.worker.controller;

import com.qw.common.annotation.RequireRole;
import com.qw.common.result.Result;
import com.qw.worker.dto.LocationRequest;
import com.qw.worker.service.IWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：qw
 * @Package：com.qw.worker.controller
 * @Project：home-serve
 * @name：WorkerController
 * @Date：2026/5/20 18:46
 * @Filename：WorkerController
 */
@RestController
@RequestMapping("/worker")
@Tag(name = "服务者模块")
public class WorkerController {
    @Autowired
    IWorkerService workerServiceImpl;

    @RequireRole({"2"})
    @Operation(summary = "获得服务者信息")
    @GetMapping("/profile")
    public Result getProfile(){
        return Result.ok(workerServiceImpl.getProfile());
    }

    @RequireRole({"2"})
    @Operation(summary = "切换在线状态")
    @PutMapping("/online")
    public Result updateOnlineStatus(@RequestParam Integer status){
        workerServiceImpl.updateOnlineStatus(status);
        return Result.ok();
    }

    @RequireRole({"2"})
    @Operation(summary = "实时更新位置")
    @PutMapping("/location")
    public Result updateLocation(@Validated @RequestBody LocationRequest locationRequest){
        workerServiceImpl.updateLocation(locationRequest);
        return Result.ok();
    }

    @RequireRole({"2"})
    @Operation(summary = "获取当前的订单")
    @GetMapping("/orders")
    public Result getMyOrders(){
        return Result.ok(workerServiceImpl.getMyOrder());
    }

    @RequireRole({"2"})
    @Operation(summary = "开始服务")
    @PutMapping("/orders/{id}/start")
    public Result startService(@PathVariable Long id){
        workerServiceImpl.startService(id);
        return Result.ok();
    }

    @RequireRole({"2"})
    @Operation(summary = "完成服务")
    @PutMapping("/orders/{id}/complete")
    public Result endService(@PathVariable Long id){
        workerServiceImpl.completeService(id);
        return Result.ok();
    }
}
