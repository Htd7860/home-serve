package com.qw.common.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qw.common.entity.Notifications;
import com.qw.common.mapper.NotificationMapper;
import com.qw.common.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.common.service.impl
 * @Project：home-serve
 * @name：NotificationServiceImpl
 * @Date：2026/5/27
 * @Filename：NotificationServiceImpl
 */
@Service
public class NotificationServiceImpl implements INotificationService {
    @Autowired
    NotificationMapper notificationMapper;

    @Override
    public List<Notifications> getNotifications(Long userId, Integer loginType, Integer page, Integer size) {
        Page<Notifications> p = new Page<>(page, size);
        return notificationMapper.listByPage(p, userId, loginType - 1);
    }

    @Override
    public void readNotification(Long id, Long userId, Integer loginType) {
        notificationMapper.readById(id, userId, loginType - 1);
    }

    @Override
    public void readAll(Long userId, Integer loginType) {
        notificationMapper.readAll(userId, loginType - 1);
    }

    @Override
    public int getUnreadCount(Long userId, Integer loginType) {
        return notificationMapper.countUnread(userId, loginType - 1);
    }
}
