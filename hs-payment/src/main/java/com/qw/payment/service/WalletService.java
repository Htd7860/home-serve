package com.qw.payment.service;

import com.qw.payment.dto.WithdrawRequest;
import com.qw.payment.entity.WorkerEarnings;
import com.qw.payment.entity.WorkerWallets;
import com.qw.payment.entity.WorkerWithdraws;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.payment.service
 * @Project：home-serve
 * @name：WalletService
 * @Date：2026/5/25 17:08
 * @Filename：WalletService
 */
public interface WalletService {

    WorkerWallets getByWorkerId(Long workerId);

    List<WorkerEarnings> getEarnings(Long userId, int page, int size);

    void withdraw(Long workerId, WithdrawRequest request);

    List<WorkerWithdraws> getWithdraws(Long userId,int page,int size);

    void settle(Long workerId, Long orderId, BigDecimal finalPrice);
}
