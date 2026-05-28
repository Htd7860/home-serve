package com.qw.payment.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qw.payment.entity.PaymentRecords;
import com.qw.payment.entity.WorkerEarnings;
import com.qw.payment.entity.WorkerWallets;
import com.qw.payment.entity.WorkerWithdraws;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

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

    @Insert("insert into worker_wallets (worker_id, balance, frozen_balance, total_earned) values (#{workId},0,0,0)")
    void initWallet(Long workId);

    @Update("update worker_wallets set balance=#{balance},total_earned=#{totalEarned},version=version+1,updated_at=NOW() where worker_id=#{workerId}" +
            " and version=#{version}")
    int addBalance(@Param("workerId") Long workerId,@Param("balance") BigDecimal balance, @Param("totalEarned")BigDecimal totalEarned, @Param("version")Integer version);

    @Insert("insert into worker_earnings (worker_id, order_id, order_price, worker_ratio, worker_amount, platform_amount) values(#{workerId},#{orderId}," +
            "#{orderPrice},#{workerRatio},#{workerAmount},#{platformAmount})")
    void insertEarning(WorkerEarnings workerEarning);

    @Select("select * from worker_wallets where worker_id=#{workerId}")
    WorkerWallets getByWorkerId(Long workerId);

    @Select("select * from worker_earnings where worker_id=#{workerId} order by created_at desc")
    List<WorkerEarnings> getByWorkerEarnings(Long workerId, Page page);

    @Update("update worker_wallets set balance=balance-#{amount},frozen_balance=frozen_balance+#{amount} ,version=version+1," +
            "updated_at=now() where worker_id=#{workerId} and version=#{version} and balance>=#{amount}" +
            "")
    int freezeBalance(Long workerId,BigDecimal amount,Integer version);

    @Insert("insert into worker_withdraws (withdraw_no, worker_id, amount, bank_name, bank_card_no, status, remark, created_at)" +
            "values(#{withdrawNo},#{workerId},#{amount},#{bankName},#{bankCardNo},#{status},#{remark},NOW()) ")
    void insertWithdraw(WorkerWithdraws workerWithdraw);

    @Select("select * from worker_withdraws where worker_id=#{workerId} order by created_at desc")
    List<WorkerWithdraws> getWithdrawsByWorkerId(Long workerId,Page page);

    @Select("select count(*) from worker_earnings where order_id = #{orderId}")
    int countEarningByOrderId(Long orderId);
}
