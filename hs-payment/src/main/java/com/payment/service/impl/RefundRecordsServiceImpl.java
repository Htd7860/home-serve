package com.qw.payment.service.impl;

import com.qw.payment.entity.RefundRecords;
import com.qw.payment.mapper.RefundRecordsMapper;
import com.qw.payment.service.IRefundRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 退款记录 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
public class RefundRecordsServiceImpl extends ServiceImpl<RefundRecordsMapper, RefundRecords> implements IRefundRecordsService {

}
