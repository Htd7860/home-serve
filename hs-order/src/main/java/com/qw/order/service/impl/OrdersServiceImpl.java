package com.qw.order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.service.ISkuService;
import com.qw.order.dto.CreateOrderRequest;
import com.qw.order.entity.Orders;
import com.qw.order.mapper.OrdersMapper;
import com.qw.order.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qw.user.entity.UserAddresses;
import com.qw.user.service.IAddressService;
import com.qw.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
@Slf4j
public class OrdersServiceImpl  implements IOrdersService {
    @Autowired
    ISkuService skuServiceImpl;
    @Autowired
    IUserService userServiceImpl;
    @Autowired
    IAddressService addressServiceImpl;
    @Autowired
    Cache<String,Object> ordersCache;


    @Transactional
    @Override
    public Orders createOrder(CreateOrderRequest req, Long userid) {
        //进行sku以及地址的校验
        ServiceSkus serviceSkus=null;
        try {
            serviceSkus=skuServiceImpl.getById(req.getSkuId());
        } catch (JsonProcessingException e) {
            log.error("{}",e);
            return null;
        }
        if(serviceSkus==null){return null;}
        Integer categoryId = serviceSkus.getCategoryId();
        UserAddresses address=addressServiceImpl.getById(req.getAddressId());
        if(address==null||!address.getUserId().equals(userid)){return null;}

        //计算价格（时段和距离加价以及优惠券条件判断）
        BigDecimal bigDecimal=serviceSkus.getBasePrice();
        BigDecimal[] bigDecimals = skuServiceImpl.calculateMoney(req.getLocalDateTime(), bigDecimal, false, null);
        BigDecimal time=bigDecimals[1];
        BigDecimal dis=BigDecimal.ZERO;
        if(req.getCouponId()!=null){

        }
        //生成地址快照

        //记录状态变更

        //改变优惠券使用状态

        return null;
    }
}
