package com.qw.payment.service.impl;

import com.qw.payment.entity.PaymentRecords;
import com.qw.payment.mapper.PaymentRecordsMapper;
import com.qw.payment.service.IPaymentRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付流水 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
public class PaymentRecordsServiceImpl extends ServiceImpl<PaymentRecordsMapper, PaymentRecords> implements IPaymentRecordsService {

}
