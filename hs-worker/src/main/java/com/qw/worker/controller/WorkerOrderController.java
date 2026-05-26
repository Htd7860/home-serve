package com.qw.worker.controller;

import com.qw.common.result.Result;
import com.qw.worker.service.IWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：qw
 * @Package：com.qw.worker.controller
 * @Project：home-serve
 * @name：WorkerOrderController
 * @Date：2026/5/21 10:43
 * @Filename：WorkerOrderController
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "服务者接单模块")
public class WorkerOrderController {
    @Autowired
    IWorkerService workerServiceImpl;

    @PostMapping("/{id}/grab")
    @Operation(summary = "抢单")
    public Result grabOrder(@PathVariable Long id){
        workerServiceImpl.grabOrder(id);
        return Result.ok();
    }
}
