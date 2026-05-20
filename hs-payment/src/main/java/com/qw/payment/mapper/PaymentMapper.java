package com.qw.payment.mapper;

import com.qw.payment.entity.PaymentRecords;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author：qw
 * @Package：com.qw.payment.mapper
 * @Project：home-serve
 * @name：PaymentMapper
 * @Date：2026/5/20 14:07
 * @Filename：PaymentMapper
 */
@Mapper
public interface PaymentMapper {
    @Insert("insert into payment_records (payment_no, order_id, user_id, amount, status, paid_at,method) " +
            "values(#{paymentNo},#{orderId},#{userId},#{amount},#{status},#{paidAt},#{method})")
    void insertPaymentRecord(PaymentRecords paymentRecords);

}
