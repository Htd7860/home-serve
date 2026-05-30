package com.qw.payment.service.impl;

import com.qw.common.exception.BizException;
import com.qw.payment.dto.WithdrawRequest;
import com.qw.payment.entity.WorkerEarnings;
import com.qw.payment.entity.WorkerWallets;
import com.qw.payment.entity.WorkerWithdraws;
import com.qw.payment.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock PaymentMapper paymentMapper;

    @InjectMocks
    WalletServiceImpl walletService;

    // ==================== getByWorkerId ====================

    @Test
    void getByWorkerId_Exists_ReturnsWallet() {
        WorkerWallets w = wallet(1L, new BigDecimal("500"), 0);
        when(paymentMapper.getByWorkerId(1L)).thenReturn(w);

        WorkerWallets result = walletService.getByWorkerId(1L);

        assertEquals(new BigDecimal("500"), result.getBalance());
        verify(paymentMapper, never()).initWallet(anyLong());
    }

    @Test
    void getByWorkerId_NotExists_InitAndReturn() {
        WorkerWallets fresh = wallet(1L, BigDecimal.ZERO, 0);
        when(paymentMapper.getByWorkerId(1L)).thenReturn(null).thenReturn(fresh);

        WorkerWallets result = walletService.getByWorkerId(1L);

        assertNotNull(result);
        verify(paymentMapper).initWallet(1L);
    }

    // ==================== getEarnings ====================

    @Test
    void getEarnings_ReturnsList() {
        List<WorkerEarnings> expected = List.of(new WorkerEarnings());
        when(paymentMapper.getByWorkerEarnings(eq(1L), any())).thenReturn(expected);

        List<WorkerEarnings> result = walletService.getEarnings(1L, 1, 10);

        assertEquals(expected, result);
    }

    @Test
    void getEarnings_Null_ReturnsEmptyList() {
        when(paymentMapper.getByWorkerEarnings(eq(1L), any())).thenReturn(null);

        List<WorkerEarnings> result = walletService.getEarnings(1L, 1, 10);

        assertTrue(result.isEmpty());
    }

    // ==================== withdraw ====================

    @Test
    void withdraw_Success() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(new BigDecimal("200"));
        req.setBankCardNo("6222021234567890");
        req.setBankName("中国工商银行");
        WorkerWallets w = wallet(1L, new BigDecimal("500"), 0);
        when(paymentMapper.getByWorkerId(1L)).thenReturn(w);
        when(paymentMapper.freezeBalance(1L, new BigDecimal("200"), 0)).thenReturn(1);

        walletService.withdraw(1L, req);

        verify(paymentMapper).freezeBalance(1L, new BigDecimal("200"), 0);
        verify(paymentMapper).insertWithdraw(any(WorkerWithdraws.class));
    }

    @Test
    void withdraw_AmountTooSmall_ThrowsBizException() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(new BigDecimal("50"));

        BizException ex = assertThrows(BizException.class, () -> walletService.withdraw(1L, req));
        assertTrue(ex.getMessage().contains("提现金额"));
    }

    @Test
    void withdraw_AmountNull_ThrowsBizException() {
        WithdrawRequest req = new WithdrawRequest();

        BizException ex = assertThrows(BizException.class, () -> walletService.withdraw(1L, req));
        assertTrue(ex.getMessage().contains("提现金额"));
    }

    @Test
    void withdraw_BalanceNotEnough_ThrowsBizException() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(new BigDecimal("600"));
        WorkerWallets w = wallet(1L, new BigDecimal("500"), 0);
        when(paymentMapper.getByWorkerId(1L)).thenReturn(w);

        BizException ex = assertThrows(BizException.class, () -> walletService.withdraw(1L, req));
        assertEquals("余额不足", ex.getMessage());
    }

    @Test
    void withdraw_OptimisticLockConflict_ThrowsBizException() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(new BigDecimal("200"));
        WorkerWallets w = wallet(1L, new BigDecimal("500"), 0);
        when(paymentMapper.getByWorkerId(1L)).thenReturn(w);
        when(paymentMapper.freezeBalance(1L, new BigDecimal("200"), 0)).thenReturn(0);

        BizException ex = assertThrows(BizException.class, () -> walletService.withdraw(1L, req));
        assertEquals("乐观锁冲突", ex.getMessage());
    }

    // ==================== getWithdraws ====================

    @Test
    void getWithdraws_ReturnsList() {
        List<WorkerWithdraws> expected = List.of(new WorkerWithdraws());
        when(paymentMapper.getWithdrawsByWorkerId(eq(1L), any())).thenReturn(expected);

        List<WorkerWithdraws> result = walletService.getWithdraws(1L, 1, 10);

        assertEquals(expected, result);
    }

    // ==================== settle ====================

    @Test
    void settle_Success_FirstTry() {
        WorkerWallets w = wallet(10L, new BigDecimal("1000"), 0);
        when(paymentMapper.getByWorkerId(10L)).thenReturn(w);
        when(paymentMapper.addBalance(eq(10L), any(), any(), eq(0))).thenReturn(1);

        walletService.settle(10L, 100L, new BigDecimal("100"), BigDecimal.ZERO);

        verify(paymentMapper).insertEarning(any(WorkerEarnings.class));
    }

    @Test
    void settle_WithDistanceFee_WorkerGetsAllDistance() {
        WorkerWallets w = wallet(10L, new BigDecimal("1000"), 0);
        when(paymentMapper.getByWorkerId(10L)).thenReturn(w);
        // workerAmount = 100 * 0.80 + 14 = 80 + 14 = 94
        when(paymentMapper.addBalance(eq(10L), any(), any(), eq(0))).thenReturn(1);

        walletService.settle(10L, 100L, new BigDecimal("100"), new BigDecimal("14"));

        verify(paymentMapper).insertEarning(argThat(e ->
                e.getWorkerAmount().compareTo(new BigDecimal("94.0")) == 0));
    }

    @Test
    void settle_NullDistanceFee_TreatsAsZero() {
        WorkerWallets w = wallet(10L, new BigDecimal("1000"), 0);
        when(paymentMapper.getByWorkerId(10L)).thenReturn(w);
        when(paymentMapper.addBalance(eq(10L), any(), any(), eq(0))).thenReturn(1);

        assertDoesNotThrow(() -> walletService.settle(10L, 100L, new BigDecimal("100"), null));
    }

    @Test
    void settle_RetryOnLockConflict_ThenSucceed() {
        WorkerWallets w = wallet(10L, new BigDecimal("1000"), 0);
        when(paymentMapper.getByWorkerId(10L)).thenReturn(w);
        when(paymentMapper.addBalance(eq(10L), any(), any(), eq(0)))
                .thenReturn(0).thenReturn(1);

        walletService.settle(10L, 100L, new BigDecimal("100"), BigDecimal.ZERO);

        verify(paymentMapper, times(2)).addBalance(eq(10L), any(), any(), eq(0));
        verify(paymentMapper).insertEarning(any());
    }

    @Test
    void settle_AllRetriesFail_ThrowsBizException() {
        WorkerWallets w = wallet(10L, new BigDecimal("1000"), 0);
        when(paymentMapper.getByWorkerId(10L)).thenReturn(w);
        when(paymentMapper.addBalance(eq(10L), any(), any(), eq(0)))
                .thenReturn(0).thenReturn(0).thenReturn(0);

        BizException ex = assertThrows(BizException.class,
                () -> walletService.settle(10L, 100L, new BigDecimal("100"), BigDecimal.ZERO));
        assertEquals("分账失败，请重试", ex.getMessage());
    }

    // ==================== 工具方法 ====================

    private WorkerWallets wallet(Long workerId, BigDecimal balance, int version) {
        WorkerWallets w = new WorkerWallets();
        w.setWorkerId(workerId);
        w.setBalance(balance);
        w.setTotalEarned(new BigDecimal("10000"));
        w.setVersion(version);
        return w;
    }
}
