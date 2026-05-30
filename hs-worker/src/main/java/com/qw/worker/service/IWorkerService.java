package com.qw.worker.service;

import com.qw.common.entity.Workers;
import com.qw.order.entity.Orders;
import com.qw.worker.dto.LocationRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.worker.service
 * @Project：home-serve
 * @name：IWokerService
 * @Date：2026/5/20 18:44
 * @Filename：IWokerService
 */
public interface IWorkerService {
    Workers getProfile();

    void updateOnlineStatus(Integer status);

    void updateLocation(LocationRequest locationRequest);

    Long grabOrder(Long orderId);

    List<Orders> getMyOrder();

    void startService(Long id);

    void completeService(Long id);

    String uploadAvatar(MultipartFile file);
}
