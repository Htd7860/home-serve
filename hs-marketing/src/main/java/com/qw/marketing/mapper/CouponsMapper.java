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

    @Select("select * from coupon_templates where type = 0 and status = 1")
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

    @Select("select * from user_coupons where user_id=#{userId} and template_id=#{templateId}")
    UserCoupons getByUserAndTemplate(@Param("userId") Long userId, @Param("templateId") Long templateId);

    @Select("select * from coupon_templates order by created_at desc")
    List<CouponTemplates> listAllTemplates();

    @Insert("insert into coupon_templates (coupon_name, coupon_type, threshold_amount, discount_amount, discount_rate, valid_days, category_id, type, status, created_at) " +
            "values (#{couponName},#{couponType},#{thresholdAmount},#{discountAmount},#{discountRate},#{validDays},#{categoryId},#{type},#{status},now())")
    int insertTemplate(CouponTemplates template);

    @Update("update coupon_templates set coupon_name=#{couponName},coupon_type=#{couponType}," +
            "threshold_amount=#{thresholdAmount},discount_amount=#{discountAmount},discount_rate=#{discountRate}," +
            "valid_days=#{validDays},category_id=#{categoryId},type=#{type},status=#{status} where id=#{id}")
    int updateTemplate(CouponTemplates template);
}
