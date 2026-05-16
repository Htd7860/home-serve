package com.qw.order.service.impl;

import com.qw.order.entity.OrderEvents;
import com.qw.order.mapper.OrderEventsMapper;
import com.qw.order.service.IOrderEventsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单状态变更流水 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
public class OrderEventsServiceImpl extends ServiceImpl<OrderEventsMapper, OrderEvents> implements IOrderEventsService {

}
