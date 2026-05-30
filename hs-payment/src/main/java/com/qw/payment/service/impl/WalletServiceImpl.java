package com.qw.payment.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qw.common.constant.WithdrawStatus;
import com.qw.common.exception.BizException;
import com.qw.common.utils.OrderNoUtils;
import com.qw.payment.dto.WithdrawRequest;
import com.qw.payment.entity.WorkerEarnings;
import com.qw.payment.entity.WorkerWallets;
import com.qw.payment.entity.WorkerWithdraws;
import com.qw.payment.mapper.PaymentMapper;
import com.qw.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.payment.service.impl
 * @Project：home-serve
 * @name：WalletServiceImpl
 * @Date：2026/5/25 17:08
 * @Filename：WalletServiceImpl
 */
@Service
public class WalletServiceImpl implements WalletService {
    @Autowired
    PaymentMapper paymentMapper;
    @Override
    public WorkerWallets getByWorkerId(Long workerId) {
        WorkerWallets wallets = paymentMapper.getByWorkerId(workerId);
        if(wallets==null){
            paymentMapper.initWallet(workerId);
            wallets=paymentMapper.getByWorkerId(workerId);
        }
        return wallets;
    }

    @Override
    public List<WorkerEarnings> getEarnings(Long userId, int page, int size) {
        Page<WorkerEarnings> pages=new Page<>(page,size);
        List<WorkerEarnings> earnings = paymentMapper.getByWorkerEarnings(userId, pages);
        if(earnings==null){return List.of();}
        return earnings;
    }

    @Transactional
    @Override
    public void withdraw(Long workerId, WithdrawRequest request) {
        if(request.getAmount()==null||request.getAmount().compareTo(new BigDecimal("100"))<0){
            throw new BizException("提现金额不能小于100元");
        }
        WorkerWallets wallet=paymentMapper.getByWorkerId(workerId);
        if(wallet==null||wallet.getBalance().compareTo(request.getAmount())<0){
            throw new BizException("余额不足");
        }

        int rows= paymentMapper.freezeBalance(workerId,request.getAmount(),wallet.getVersion());
        if(rows==0){
            throw new BizException("乐观锁冲突");
        }

        WorkerWithdraws workerWithdraws=WorkerWithdraws.builder().workerId(workerId).amount(request.getAmount())
                .bankCardNo(request.getBankCardNo()).bankName(request.getBankName()).status(WithdrawStatus.PROCESSING.getCode()).remark("提现记录").withdrawNo("WD:"+ OrderNoUtils.generateOrderNo()).build();
        paymentMapper.insertWithdraw(workerWithdraws);
    }

    @Override
    public List<WorkerWithdraws> getWithdraws(Long userId, int page, int size) {
        Page<WorkerWithdraws> pages=new Page(page,size);
       return  paymentMapper.getWithdrawsByWorkerId(userId,pages);
    }

    @Transactional
    @Override
    public void settle(Long workerId, Long orderId, BigDecimal finalPrice, BigDecimal distanceFee) {
        if (paymentMapper.countEarningByOrderId(orderId) > 0) {
            return;
        }
        BigDecimal workerRatio=new BigDecimal("0.80");
        BigDecimal distanceFeeSafe = distanceFee != null ? distanceFee : BigDecimal.ZERO;
        BigDecimal workerAmount=finalPrice.multiply(workerRatio).add(distanceFeeSafe);
        BigDecimal platformAmount=finalPrice.subtract(finalPrice.multiply(workerRatio));

        WorkerWallets wallet = this.getByWorkerId(workerId);
        for(int i=0;i<3;i++){
            BigDecimal newBalance=wallet.getBalance().add(workerAmount);
            BigDecimal totalEarned=wallet.getTotalEarned().add(workerAmount);
            int rows=paymentMapper.addBalance(workerId,newBalance,totalEarned,wallet.getVersion());
            if(rows==1){
                WorkerEarnings workerEarnings=WorkerEarnings.builder().workerId(workerId).orderId(orderId)
                        .platformAmount(platformAmount).workerRatio(workerRatio).orderPrice(finalPrice).workerAmount(workerAmount).build();
                paymentMapper.insertEarning(workerEarnings);
                return;
            }
            wallet = paymentMapper.getByWorkerId(workerId);
        }
        throw new BizException("分账失败，请重试");
    }


}
