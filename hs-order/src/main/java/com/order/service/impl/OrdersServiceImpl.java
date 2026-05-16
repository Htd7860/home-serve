package com.qw.order.service.impl;

import com.qw.order.entity.Orders;
import com.qw.order.mapper.OrdersMapper;
import com.qw.order.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

}
