package com.qw.marketing.service.impl;

import com.qw.common.cache.CacheTemplate;
import com.qw.marketing.constant.CouponsConstant;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;
import com.qw.marketing.mapper.CouponsMapper;
import com.qw.marketing.service.ICouponService;
import com.qw.common.result.Result;
import com.qw.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 优惠券模板 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
@Slf4j
public class CouponServiceImpl implements ICouponService {

    @Autowired
    CouponsMapper couponsMapper;
    @Autowired
    CacheTemplate cacheTemplate;
    @Override
    public List<UserCoupons> getMyCoupons(Integer status) {
        return couponsMapper.getCouponsByStatus(status, UserContext.getUserId());
    }

    @Override
    public List<CouponTemplates> getAvailableCoupons() {
        return   cacheTemplate.getList(CouponsConstant.COUPONS_AVAILABLE_KEY,null,CouponTemplates.class,key->couponsMapper.getAvailableCoupons());
    }

    @Transactional
    @Override
    public Result receiveCoupons(Long templateId) {

        CouponTemplates templates = couponsMapper.getTemplatesById(templateId);
        Integer receivedCount = templates.getReceivedCount();Integer total=templates.getTotalQuantity();
        if(!(receivedCount<total)){
            return Result.fail(CouponsConstant.COUPONS_STORE_NOT_ENOUGH);
        };

        if(templates.getStatus()==0){
            return Result.fail(CouponsConstant.COUPONS_DISABLED);
        }
        LocalDateTime effectiveTime=LocalDateTime.now().plusDays(templates.getValidDays());
        couponsMapper.addCouponsRecord(templateId);
        UserCoupons userCoupons=UserCoupons.builder().userId(UserContext.getUserId()).createdAt(LocalDateTime.now())
                        .status(0).expireTime(effectiveTime).templateId(templateId).build();
        try {
            couponsMapper.receiveCoupon(userCoupons);
        } catch (DuplicateKeyException e) {
            log.error("{}",e);
            return Result.fail(CouponsConstant.COUPONS_DUPLICATE_RECEIVE);
        }
        cacheTemplate.clear(CouponsConstant.COUPONS_AVAILABLE_KEY);
        return Result.ok();
    }
}
