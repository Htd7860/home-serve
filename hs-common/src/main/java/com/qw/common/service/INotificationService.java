package com.qw.common.service;

import com.qw.common.entity.Notifications;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.common.service
 * @Project：home-serve
 * @name：INotificationService
 * @Date：2026/5/27
 * @Filename：INotificationService
 */
public interface INotificationService {
    List<Notifications> getNotifications(Long userId, Integer loginType, Integer page, Integer size);

    void readNotification(Long id, Long userId, Integer loginType);

    void readAll(Long userId, Integer loginType);

    int getUnreadCount(Long userId, Integer loginType);
}
