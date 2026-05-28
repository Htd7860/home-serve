package com.qw.common.controller;

import com.qw.common.annotation.RequireRole;
import com.qw.common.result.Result;
import com.qw.common.service.INotificationService;
import com.qw.common.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：qw
 * @Package：com.qw.common.controller
 * @Project：home-serve
 * @name：NotificationController
 * @Date：2026/5/27
 * @Filename：NotificationController
 */
@RestController
@RequestMapping("/notifications")
@Tag(name = "通知模块")
public class NotificationController {
    @Autowired
    INotificationService notificationServiceImpl;

    @RequireRole({"1", "2"})
    @GetMapping
    @Operation(summary = "查通知列表")
    public Result getNotifications(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(notificationServiceImpl.getNotifications(
                UserContext.getUserId(), UserContext.getLoginType(), page, size));
    }

    @RequireRole({"1", "2"})
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public Result readNotification(@PathVariable Long id) {
        notificationServiceImpl.readNotification(id, UserContext.getUserId(), UserContext.getLoginType());
        return Result.ok();
    }

    @RequireRole({"1", "2"})
    @PutMapping("/read-all")
    @Operation(summary = "全部已读")
    public Result readAll() {
        notificationServiceImpl.readAll(UserContext.getUserId(), UserContext.getLoginType());
        return Result.ok();
    }

    @RequireRole({"1", "2"})
    @GetMapping("/unread-count")
    @Operation(summary = "查未读数量")
    public Result getUnreadCount() {
        return Result.ok(notificationServiceImpl.getUnreadCount(
                UserContext.getUserId(), UserContext.getLoginType()));
    }
}
