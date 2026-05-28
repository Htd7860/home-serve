package com.qw.order.mapper;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qw.order.entity.OrderAddressSnapshots;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mockito.internal.matchers.Or;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;

/**
 * <p>
 * 订单主表 Mapper 接口
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Mapper
public interface OrdersMapper{

    Long insertOrders(Orders orders);

    @Insert("insert into order_events (order_id, from_status, to_status, operator_type, operator_id,remark,created_at)" +
            "values(#{orderId},#{fromStatus},#{toStatus},#{operatorType},#{operatorId},#{remark},#{createdAt}) ")
    int insertOrderEvent(OrderEvents orderEvents);

    @Insert("insert into order_address_snapshots (order_id, contact_name, contact_phone, full_address, lng, lat) " +
            "values (#{orderId},#{contactName},#{contactPhone},#{fullAddress},#{lng},#{lat})")
    int insertAddressSnapshots(OrderAddressSnapshots snapshots);


    List<Orders> getMyOrders(@Param("status") Integer status, @Param("userId") Long userId, Page<Orders> pages);

    @Select("select * from orders where id=#{id}")
    Orders getOrderById(Long id);

    @Select("select * from order_events where order_id=#{id}")
    List<OrderEvents> getOrderEventByOrderId(Long id);

    int updateOrders(Orders orders);

    @Select("select * from orders where worker_id=#{id} order by created_at desc")
    List<Orders> getOrdersByWorkerId(Long id);

    @Select("select * from order_address_snapshots where order_id=#{orderId}")
    OrderAddressSnapshots getOrderAddressSnapshotsByOrderId(Long orderId);
}
