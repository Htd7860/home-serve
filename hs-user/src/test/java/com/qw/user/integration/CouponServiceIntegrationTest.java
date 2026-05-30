package com.qw.user.integration;

import com.qw.common.exception.BizException;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;
import com.qw.marketing.service.ICouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CouponServiceIntegrationTest {

    @MockBean StringRedisTemplate stringRedisTemplate;
    @MockBean RedissonClient redissonClient;
    @MockBean(name = "skuBloomFilter")
    RBloomFilter<Object> skuBloomFilter;
    @MockBean(name = "categoryBloomFilter")
    RBloomFilter<Object> categoryBloomFilter;
    @MockBean RocketMQTemplate rocketMQTemplate;

    @Autowired ICouponService couponService;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM user_coupons");
        jdbcTemplate.update("DELETE FROM coupon_templates");
    }

    // ==================== getAvailableCoupons ====================

    @Test
    void getAvailableCoupons_ReturnsActiveTemplates() {
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (coupon_name, coupon_type, threshold_amount, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?,?)",
                "新人满100减20", 1, new BigDecimal("100"), new BigDecimal("20"), 30, 0, 1);
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (coupon_name, coupon_type, discount_rate, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?)",
                "全场8折", 2, new BigDecimal("0.85"), 7, 0, 1);

        List<CouponTemplates> result = couponService.getAvailableCoupons();

        assertEquals(2, result.size());
    }

    @Test
    void getAvailableCoupons_NoActive_ReturnsEmpty() {
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (coupon_name, coupon_type, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?)",
                "已禁用券", 1, new BigDecimal("10"), 30, 0, 0);

        List<CouponTemplates> result = couponService.getAvailableCoupons();

        assertTrue(result.isEmpty());
    }

    // ==================== receiveCoupon ====================

    @Test
    void receiveCoupon_Success() {
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (id, coupon_name, coupon_type, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?,?)",
                1L, "新人券", 1, new BigDecimal("20"), 30, 0, 1);

        assertDoesNotThrow(() -> couponService.receiveCoupon(1L, 100L));

        List<UserCoupons> myCoupons = couponService.getMyCoupons(100L, null);
        assertEquals(1, myCoupons.size());
        assertEquals(1L, myCoupons.get(0).getTemplateId());
    }

    @Test
    void receiveCoupon_Duplicate_ThrowsBizException() {
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (id, coupon_name, coupon_type, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?,?)",
                1L, "新人券", 1, new BigDecimal("20"), 30, 0, 1);

        couponService.receiveCoupon(1L, 100L);

        assertThrows(BizException.class, () -> couponService.receiveCoupon(1L, 100L));
    }

    @Test
    void receiveCoupon_TemplateNotExists_ThrowsBizException() {
        assertThrows(BizException.class, () -> couponService.receiveCoupon(999L, 100L));
    }

    @Test
    void receiveCoupon_TemplateDisabled_ThrowsBizException() {
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (id, coupon_name, coupon_type, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?,?)",
                1L, "已禁用", 1, new BigDecimal("20"), 30, 0, 0);

        assertThrows(BizException.class, () -> couponService.receiveCoupon(1L, 100L));
    }

    // ==================== getMyCoupons ====================

    @Test
    void getMyCoupons_ReturnsAll() {
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (id, coupon_name, coupon_type, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?,?)",
                1L, "券A", 1, new BigDecimal("20"), 30, 0, 1);
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (id, coupon_name, coupon_type, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?,?)",
                2L, "券B", 1, new BigDecimal("10"), 30, 0, 1);

        couponService.receiveCoupon(1L, 100L);
        couponService.receiveCoupon(2L, 100L);

        List<UserCoupons> result = couponService.getMyCoupons(100L, null);

        assertEquals(2, result.size());
    }

    @Test
    void getMyCoupons_FilterByStatus() {
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (id, coupon_name, coupon_type, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?,?)",
                1L, "券A", 1, new BigDecimal("20"), 30, 0, 1);

        couponService.receiveCoupon(1L, 100L);

        // status=0 = UNUSED
        List<UserCoupons> unused = couponService.getMyCoupons(100L, 0);
        assertEquals(1, unused.size());

        // status=1 = USED
        List<UserCoupons> used = couponService.getMyCoupons(100L, 1);
        assertTrue(used.isEmpty());
    }
}
