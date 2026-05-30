package com.qw.user.integration;

import com.qw.common.constant.OrderStatus;
import com.qw.common.constant.PayStatus;
import com.qw.common.exception.BizException;
import com.qw.common.utils.UserContext;
import com.qw.order.dto.CreateOrderRequest;
import com.qw.order.dto.OrderDetailResponse;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.qw.order.service.IOrdersService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class OrdersServiceIntegrationTest {

    @MockBean StringRedisTemplate stringRedisTemplate;
    @MockBean RedissonClient redissonClient;
    @MockBean(name = "skuBloomFilter")
    RBloomFilter<Object> skuBloomFilter;
    @MockBean(name = "categoryBloomFilter")
    RBloomFilter<Object> categoryBloomFilter;
    @MockBean RocketMQTemplate rocketMQTemplate;

    @Autowired IOrdersService ordersService;
    @Autowired JdbcTemplate jdbcTemplate;

    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM order_events");
        jdbcTemplate.update("DELETE FROM order_address_snapshots");
        jdbcTemplate.update("DELETE FROM payment_records");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM user_addresses");
        jdbcTemplate.update("DELETE FROM service_skus");
        jdbcTemplate.update("DELETE FROM pricing_rules");
        jdbcTemplate.update("DELETE FROM user_coupons");
        jdbcTemplate.update("DELETE FROM coupon_templates");
        UserContext.set(1, TEST_USER_ID);

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = org.mockito.Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(ops);
        when(skuBloomFilter.contains(anyLong())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== createOrder ====================

    @Test
    void createOrder_Success() {
        // 准备 SKU
        jdbcTemplate.update(
                "INSERT INTO service_skus (id, category_id, name, base_price, status) VALUES (?,?,?,?,?)",
                1L, 1, "保洁-2小时", new BigDecimal("99"), 1);
        // 准备地址
        insertTestAddress(TEST_USER_ID, 1L, "张三", "13800138000",
                "浙江省", "杭州市", "西湖区", "文三路100号",
                new BigDecimal("120.123"), new BigDecimal("30.123"));

        CreateOrderRequest req = createOrderReq(1L, 1L, LocalDateTime.now().plusDays(1));
        assertDoesNotThrow(() -> ordersService.createOrder(req, TEST_USER_ID));

        // 验证订单入库
        List<Orders> orders = jdbcTemplate.query(
                "SELECT * FROM orders WHERE user_id = ?",
                (rs, rowNum) -> Orders.builder()
                        .id(rs.getLong("id"))
                        .skuId(rs.getLong("sku_id"))
                        .userId(rs.getLong("user_id"))
                        .status(rs.getInt("status"))
                        .build(),
                TEST_USER_ID);
        assertEquals(1, orders.size());

        // 验证地址快照入库
        Integer addrCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM order_address_snapshots WHERE order_id = ?",
                Integer.class, orders.get(0).getId());
        assertEquals(1, addrCount);

        // 验证订单事件入库
        Integer eventCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM order_events WHERE order_id = ?",
                Integer.class, orders.get(0).getId());
        assertEquals(1, eventCount);
    }

    @Test
    void createOrder_WithCoupon_AppliesDiscount() {
        jdbcTemplate.update(
                "INSERT INTO service_skus (id, category_id, name, base_price, status) VALUES (?,?,?,?,?)",
                1L, 1, "保洁-2小时", new BigDecimal("100"), 1);
        insertTestAddress(TEST_USER_ID, 1L, "张三", "13800138000",
                "浙江省", "杭州市", "西湖区", "文三路100号",
                new BigDecimal("120.123"), new BigDecimal("30.123"));
        // 创建满减券模板
        jdbcTemplate.update(
                "INSERT INTO coupon_templates (id, coupon_name, coupon_type, threshold_amount, discount_amount, valid_days, type, status) " +
                "VALUES (?,?,?,?,?,?,?,?)",
                10L, "满100减30", 1, new BigDecimal("100"), new BigDecimal("30"), 30, 0, 1);
        // 给用户发券
        jdbcTemplate.update(
                "INSERT INTO user_coupons (id, user_id, template_id, status, expire_time) " +
                "VALUES (?,?,?,?,?)",
                100L, TEST_USER_ID, 10L, 0, LocalDateTime.now().plusDays(30));

        CreateOrderRequest req = createOrderReq(1L, 1L, LocalDateTime.now().plusDays(1));
        req.setCouponId(100L);

        ordersService.createOrder(req, TEST_USER_ID);

        List<Orders> orders = jdbcTemplate.query(
                "SELECT * FROM orders WHERE user_id = ?",
                (rs, rowNum) -> Orders.builder()
                        .finalPrice(rs.getBigDecimal("final_price"))
                        .couponDiscount(rs.getBigDecimal("coupon_discount"))
                        .build(),
                TEST_USER_ID);
        assertEquals(1, orders.size());
        assertEquals(new BigDecimal("70.00"), orders.get(0).getFinalPrice());
    }

    @Test
    void createOrder_Urgent_AddsUrgentFee() {
        jdbcTemplate.update(
                "INSERT INTO service_skus (id, category_id, name, base_price, status) VALUES (?,?,?,?,?)",
                1L, 1, "保洁-2小时", new BigDecimal("100"), 1);
        insertTestAddress(TEST_USER_ID, 1L, "张三", "13800138000",
                "浙江省", "杭州市", "西湖区", "文三路100号",
                new BigDecimal("120.123"), new BigDecimal("30.123"));

        CreateOrderRequest req = createOrderReq(1L, 1L, LocalDateTime.now().plusDays(1));
        req.setIsUrgent(1);

        ordersService.createOrder(req, TEST_USER_ID);

        List<Orders> orders = jdbcTemplate.query(
                "SELECT * FROM orders WHERE user_id = ?",
                (rs, rowNum) -> Orders.builder()
                        .finalPrice(rs.getBigDecimal("final_price"))
                        .urgentFee(rs.getBigDecimal("urgent_fee"))
                        .build(),
                TEST_USER_ID);
        assertEquals(1, orders.size());
        // 100 + 100*0.2 = 120
        assertEquals(new BigDecimal("120.00"), orders.get(0).getFinalPrice());
        assertEquals(new BigDecimal("20.00"), orders.get(0).getUrgentFee());
    }

    @Test
    void createOrder_SkuNotExists_ThrowsBizException() {
        insertTestAddress(TEST_USER_ID, 1L, "张三", "13800138000",
                "浙江省", "杭州市", "西湖区", "文三路100号",
                new BigDecimal("120.123"), new BigDecimal("30.123"));

        CreateOrderRequest req = createOrderReq(999L, 1L, LocalDateTime.now().plusDays(1));
        assertThrows(BizException.class, () -> ordersService.createOrder(req, TEST_USER_ID));
    }

    @Test
    void createOrder_AddressNotExists_ThrowsBizException() {
        jdbcTemplate.update(
                "INSERT INTO service_skus (id, category_id, name, base_price, status) VALUES (?,?,?,?,?)",
                1L, 1, "保洁-2小时", new BigDecimal("99"), 1);

        CreateOrderRequest req = createOrderReq(1L, 999L, LocalDateTime.now().plusDays(1));
        assertThrows(BizException.class, () -> ordersService.createOrder(req, TEST_USER_ID));
    }

    @Test
    void createOrder_AppointTimeTooEarly_ThrowsBizException() {
        jdbcTemplate.update(
                "INSERT INTO service_skus (id, category_id, name, base_price, status) VALUES (?,?,?,?,?)",
                1L, 1, "保洁-2小时", new BigDecimal("99"), 1);
        insertTestAddress(TEST_USER_ID, 1L, "张三", "13800138000",
                "浙江省", "杭州市", "西湖区", "文三路100号",
                new BigDecimal("120.123"), new BigDecimal("30.123"));

        // 预约时间在 30 分钟内
        CreateOrderRequest req = createOrderReq(1L, 1L, LocalDateTime.now().plusMinutes(10));
        assertThrows(BizException.class, () -> ordersService.createOrder(req, TEST_USER_ID));
    }

    // ==================== getMyOrders ====================

    @Test
    void getMyOrders_ReturnsList() {
        insertTestOrder(100L, TEST_USER_ID, OrderStatus.WAITING.getCode());
        insertTestOrder(200L, TEST_USER_ID, OrderStatus.GRABBED.getCode());

        List<Orders> result = ordersService.getMyOrders(null, TEST_USER_ID, 1, 10);

        assertEquals(2, result.size());
    }

    @Test
    void getMyOrders_FilterByStatus() {
        insertTestOrder(100L, TEST_USER_ID, OrderStatus.WAITING.getCode());
        insertTestOrder(200L, TEST_USER_ID, OrderStatus.GRABBED.getCode());

        List<Orders> waiting = ordersService.getMyOrders(OrderStatus.WAITING.getCode(), TEST_USER_ID, 1, 10);

        assertEquals(1, waiting.size());
        assertEquals(OrderStatus.WAITING.getCode(), waiting.get(0).getStatus());
    }

    // ==================== getOrderDetail ====================

    @Test
    void getOrderDetail_Exists_ReturnsOrderWithEvents() {
        insertTestOrder(100L, TEST_USER_ID, OrderStatus.WAITING.getCode());

        // 插入关联事件
        jdbcTemplate.update(
                "INSERT INTO order_events (order_id, from_status, to_status, operator_type, remark) VALUES (?,?,?,?,?)",
                100L, -1, OrderStatus.WAITING.getCode(), 3, "创建订单");

        OrderDetailResponse detail = ordersService.getOrderDetail(100L);

        assertNotNull(detail);
        assertEquals(100L, detail.getOrders().getId());
        assertEquals(1, detail.getOrderEvents().size());
    }

    @Test
    void getOrderDetail_NotOwned_ThrowsBizException() {
        insertTestOrder(100L, 999L, OrderStatus.WAITING.getCode());

        assertThrows(BizException.class, () -> ordersService.getOrderDetail(100L));
    }

    // ==================== cancelOrder ====================

    @Test
    void cancelOrder_WaitingStatus_Success() {
        insertTestOrder(100L, TEST_USER_ID, OrderStatus.WAITING.getCode());

        assertDoesNotThrow(() -> ordersService.cancelOrder(100L));

        Orders order = jdbcTemplate.queryForObject(
                "SELECT * FROM orders WHERE id = 100",
                (rs, rowNum) -> Orders.builder().status(rs.getInt("status")).build());
        assertEquals(OrderStatus.CANCELLED.getCode(), order.getStatus());
    }

    @Test
    void cancelOrder_NotWaiting_ThrowsBizException() {
        insertTestOrder(100L, TEST_USER_ID, OrderStatus.GRABBED.getCode());

        assertThrows(BizException.class, () -> ordersService.cancelOrder(100L));
    }

    @Test
    void cancelOrder_WithPayment_Refunds() {
        insertTestOrderWithPayStatus(100L, TEST_USER_ID, OrderStatus.WAITING.getCode(), PayStatus.PAID.getCode());

        ordersService.cancelOrder(100L);

        Orders order = jdbcTemplate.queryForObject(
                "SELECT * FROM orders WHERE id = 100",
                (rs, rowNum) -> Orders.builder()
                        .status(rs.getInt("status"))
                        .payStatus(rs.getInt("pay_status"))
                        .build());
        assertEquals(OrderStatus.CANCELLED.getCode(), order.getStatus());
        assertEquals(PayStatus.REFUNDED.getCode(), order.getPayStatus());
    }

    // ==================== refund ====================

    @Test
    void refund_WaitingAndPaid_Success() {
        insertTestOrderWithPayStatus(100L, TEST_USER_ID, OrderStatus.WAITING.getCode(), PayStatus.PAID.getCode());

        ordersService.refund(TEST_USER_ID, 100L);

        Orders order = jdbcTemplate.queryForObject(
                "SELECT * FROM orders WHERE id = 100",
                (rs, rowNum) -> Orders.builder()
                        .status(rs.getInt("status"))
                        .payStatus(rs.getInt("pay_status"))
                        .build());
        assertEquals(OrderStatus.REFUNDED.getCode(), order.getStatus());
    }

    @Test
    void refund_AlreadyGrabbed_ThrowsBizException() {
        insertTestOrder(100L, TEST_USER_ID, OrderStatus.GRABBED.getCode());

        assertThrows(BizException.class, () -> ordersService.refund(TEST_USER_ID, 100L));
    }

    // ==================== getOrderEvent ====================

    @Test
    void getOrderEvent_ReturnsEvents() {
        insertTestOrder(100L, TEST_USER_ID, OrderStatus.WAITING.getCode());
        jdbcTemplate.update(
                "INSERT INTO order_events (order_id, from_status, to_status, operator_type, remark) VALUES (?,?,?,?,?)",
                100L, -1, OrderStatus.WAITING.getCode(), 3, "创建");
        jdbcTemplate.update(
                "INSERT INTO order_events (order_id, from_status, to_status, operator_type, remark) VALUES (?,?,?,?,?)",
                100L, OrderStatus.WAITING.getCode(), OrderStatus.GRABBED.getCode(), 2, "抢单");

        List<OrderEvents> events = ordersService.getOrderEvent(100L);

        assertEquals(2, events.size());
    }

    // ==================== helper methods ====================

    private void insertTestAddress(Long userId, Long id, String name, String phone,
                                    String province, String city, String district, String detail,
                                    BigDecimal lng, BigDecimal lat) {
        jdbcTemplate.update(
                "INSERT INTO user_addresses (id, user_id, contact_name, contact_phone, province, city, district, detail, lng, lat) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)",
                id, userId, name, phone, province, city, district, detail, lng, lat);
    }

    private void insertTestOrder(Long id, Long userId, Integer status) {
        jdbcTemplate.update(
                "INSERT INTO orders (id, order_no, user_id, sku_id, category_id, address_id, status, base_price, " +
                "distance_fee, time_surcharge, coupon_discount, final_price, urgent_fee, pay_status, pay_method, " +
                "is_urgent, user_remark, created_at, appointment_time) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id, "ORD" + id, userId, 1L, 1, 1L, status,
                new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("100"), BigDecimal.ZERO, PayStatus.UNPAID.getCode(), 1,
                0, "", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    }

    private void insertTestOrderWithPayStatus(Long id, Long userId, Integer status, Integer payStatus) {
        jdbcTemplate.update(
                "INSERT INTO orders (id, order_no, user_id, sku_id, category_id, address_id, status, base_price, " +
                "distance_fee, time_surcharge, coupon_discount, final_price, urgent_fee, pay_status, pay_method, " +
                "is_urgent, user_remark, created_at, appointment_time) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id, "ORD" + id, userId, 1L, 1, 1L, status,
                new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("100"), BigDecimal.ZERO, payStatus, 1,
                0, "", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    }

    private CreateOrderRequest createOrderReq(Long skuId, Long addressId, LocalDateTime appointedTime) {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setSkuId(skuId);
        req.setAddressId(addressId);
        req.setAppointedTime(appointedTime);
        return req;
    }
}
