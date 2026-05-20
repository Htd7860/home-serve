package com.qw.marketing.mapper;

import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.marketing.mapper
 * @Project：home-serve
 * @name：CouponsMapper
 * @Date：2026/5/19 13:16
 * @Filename：CouponsMapper
 */
@Mapper
public interface CouponsMapper {
    List<UserCoupons> getCouponsByStatus(@Param("status") Integer status, @Param("userId") Long userId);

    @Select("select * from coupon_templates where total_quantity > received_count")
    List<CouponTemplates> getAvailableCoupons();

    @Update("update coupon_templates set received_count=received_count+1 where received_count< total_quantity and id=#{id}")
    void addCouponsRecord(Long id);

    @Insert("insert into user_coupons (user_id, template_id, status, expire_time,created_at) values " +
            "(#{userId},#{templateId},#{status},#{expireTime},#{createdAt})")
    void receiveCoupon(UserCoupons userCoupons);

    @Select("select * from coupon_templates where id=#{id}")
    CouponTemplates getTemplatesById(Long id);

    @Select("select * from user_coupons where id=#{id} and user_id=#{userId}")
    UserCoupons getUserCouponsByid(Long id,Long userId);

    @Update("update user_coupons set status=1,user_order_id=#{orderId} where id=#{couponId}")
    void deleteUserCouponsById(Long couponId,Long orderId);
}
