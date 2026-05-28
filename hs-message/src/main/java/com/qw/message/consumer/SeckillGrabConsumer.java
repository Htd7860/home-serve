package com.qw.message.consumer;

import com.qw.common.constant.CouponStatus;
import com.qw.common.constant.RocketMQConstant;
import com.qw.marketing.dto.SeckillGrabMessage;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;
import com.qw.marketing.mapper.CouponsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @Author：qw
 * @Package：com.qw.message.consumer
 * @Project：home-serve
 * @name：SeckillGrabConsumer
 * @Date：2026/5/26 16:26
 * @Filename：SeckillGrabConsumer
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = RocketMQConstant.SECKILL_TOPIC,selectorExpression = RocketMQConstant.SECKILL_GRAB_TAG,
consumerGroup = "seckill-grab-consumer-group")
public class SeckillGrabConsumer implements RocketMQListener<SeckillGrabMessage> {

    @Autowired
    CouponsMapper couponsMapper;
    @Override
    public void onMessage(SeckillGrabMessage seckillGrabMessage) {
        Long templateId=seckillGrabMessage.getTemplateId();
        Long id=seckillGrabMessage.getUserId();
        UserCoupons coupons = couponsMapper.getByUserAndTemplate(id, templateId);
        if(coupons!=null){return;}
        CouponTemplates templates = couponsMapper.getTemplatesById(templateId);
        UserCoupons userCoupons=UserCoupons.builder().userId(seckillGrabMessage.getUserId()).status(CouponStatus.UNUSED.getCode()).expireTime(LocalDateTime.now().plus(templates.getValidDays(), ChronoUnit.DAYS)).createdAt(LocalDateTime.now()).templateId(templateId).build();
        try {
            couponsMapper.receiveCoupon(userCoupons);
            couponsMapper.addCouponsRecord(templateId);
        } catch (Exception e) {
            log.error("{}",e);
            throw e;
        }
    }
}
