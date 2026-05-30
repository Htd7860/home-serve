package com.qw.order.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.service.ISkuService;
import com.qw.common.constant.CouponStatus;
import com.qw.common.constant.OrderStatus;
import com.qw.common.constant.PayStatus;
import com.qw.common.entity.UserAddresses;
import com.qw.common.exception.BizException;
import com.qw.common.service.IUserService;
import com.qw.common.utils.OrderNoUtils;
import com.qw.common.utils.UserContext;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;
import com.qw.marketing.mapper.CouponsMapper;
import com.qw.order.constant.ErrorConstant;
import com.qw.order.dto.CreateOrderRequest;
import com.qw.order.entity.OrderAddressSnapshots;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.qw.order.mapper.OrdersMapper;
import com.qw.payment.entity.PaymentRecords;
import com.qw.payment.mapper.PaymentMapper;
import com.qw.payment.service.WalletService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersServiceImplTest {

    @Mock ISkuService skuServiceImpl;
    @Mock IUserService userServiceImpl;
    @Mock Cache<String, Object> ordersCache;
    @Mock CouponsMapper couponsMapper;
    @Mock OrdersMapper ordersMapper;
    @Mock PaymentMapper paymentMapper;
    @Mock StringRedisTemplate stringRedisTemplate;
    @Mock RocketMQTemplate rocketMQTemplate;
    @Mock WalletService walletService;
    @Mock ValueOperations<String, String> valueOperations;

    @InjectMocks
    OrdersServiceImpl ordersService;

    MockedStatic<UserContext> userContextMock;
    MockedStatic<OrderNoUtils> orderNoUtilsMock;

    @BeforeEach
    void setUp() {
        userContextMock = mockStatic(UserContext.class);
        userContextMock.when(UserContext::getUserId).thenReturn(1L);
        orderNoUtilsMock = mockStatic(OrderNoUtils.class);
        orderNoUtilsMock.when(OrderNoUtils::generateOrderNo).thenReturn("20260529001");
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        userContextMock.close();
        orderNoUtilsMock.close();
    }

    // ==================== createOrder ====================

    @Test
    void createOrder_SkuNotFound_ThrowsBizException() {
        CreateOrderRequest req = createReq(999L, 1L, LocalDateTime.now().plusHours(2));
        when(skuServiceImpl.getById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> ordersService.createOrder(req, 1L));
        assertEquals(ErrorConstant.SERVICE_NOT_FIND, ex.getMessage());
    }

    @Test
    void createOrder_AddressNotFound_ThrowsBizException() {
        CreateOrderRequest req = createReq(10L, 999L, LocalDateTime.now().plusHours(2));
        ServiceSkus sku = sku(10L, 1, new BigDecimal("100"));
        when(skuServiceImpl.getById(10L)).thenReturn(sku);
        when(userServiceImpl.getAddressById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> ordersService.createOrder(req, 1L));
        assertEquals(ErrorConstant.ADDRESS_NOT_FIND, ex.getMessage());
    }

    @Test
    void createOrder_AppointTimeTooEarly_ThrowsBizException() {
        CreateOrderRequest req = createReq(10L, 1L, LocalDateTime.now().plusMinutes(10));
        ServiceSkus sku = sku(10L, 1, new BigDecimal("100"));
        UserAddresses addr = address(1L);
        when(skuServiceImpl.getById(10L)).thenReturn(sku);
        when(userServiceImpl.getAddressById(1L)).thenReturn(addr);

        BizException ex = assertThrows(BizException.class, () -> ordersService.createOrder(req, 1L));
        assertEquals(ErrorConstant.APPOINT_TIME_ERROR, ex.getMessage());
    }

    @Test
    void createOrder_NoCouponNoUrgent_Success() {
        CreateOrderRequest req = createReq(10L, 1L, LocalDateTime.now().plusHours(2));
        ServiceSkus sku = sku(10L, 1, new BigDecimal("100"));
        UserAddresses addr = address(1L);
        when(skuServiceImpl.getById(10L)).thenReturn(sku);
        when(userServiceImpl.getAddressById(1L)).thenReturn(addr);
        when(skuServiceImpl.calculateMoney(any(), any(), eq(false), isNull()))
                .thenReturn(new BigDecimal[]{new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO});

        ordersService.createOrder(req, 1L);

        verify(ordersMapper).insertOrders(argThat(o ->
                o.getFinalPrice().compareTo(new BigDecimal("100")) == 0 &&
                o.getUserId().equals(1L)));
        verify(ordersMapper).insertAddressSnapshots(any());
        verify(ordersMapper).insertOrderEvent(any());
    }

    @Test
    void createOrder_WithFixedCoupon_Success() {
        CreateOrderRequest req = createReq(10L, 1L, LocalDateTime.now().plusHours(2));
        req.setCouponId(5L);
        ServiceSkus sku = sku(10L, 1, new BigDecimal("100"));
        UserAddresses addr = address(1L);
        UserCoupons coupon = UserCoupons.builder().id(5L).templateId(1L).userId(1L)
                .status(CouponStatus.UNUSED.getCode()).expireTime(LocalDateTime.now().plusDays(1)).build();
        CouponTemplates template = CouponTemplates.builder().id(1L).couponType(1)
                .thresholdAmount(new BigDecimal("80")).discountAmount(new BigDecimal("20")).build();

        when(skuServiceImpl.getById(10L)).thenReturn(sku);
        when(userServiceImpl.getAddressById(1L)).thenReturn(addr);
        when(skuServiceImpl.calculateMoney(any(), any(), eq(false), isNull()))
                .thenReturn(new BigDecimal[]{new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO});
        when(couponsMapper.getUserCouponsByid(5L, 1L)).thenReturn(coupon);
        when(couponsMapper.getTemplatesById(1L)).thenReturn(template);

        ordersService.createOrder(req, 1L);

        verify(ordersMapper).insertOrders(argThat(o ->
                o.getFinalPrice().compareTo(new BigDecimal("80")) == 0 &&
                o.getCouponDiscount().compareTo(new BigDecimal("20")) == 0));
    }

    @Test
    void createOrder_WithRateCoupon_Success() {
        CreateOrderRequest req = createReq(10L, 1L, LocalDateTime.now().plusHours(2));
        req.setCouponId(5L);
        ServiceSkus sku = sku(10L, 1, new BigDecimal("100"));
        UserAddresses addr = address(1L);
        UserCoupons coupon = UserCoupons.builder().id(5L).templateId(1L).userId(1L)
                .status(CouponStatus.UNUSED.getCode()).expireTime(LocalDateTime.now().plusDays(1)).build();
        CouponTemplates template = CouponTemplates.builder().id(1L).couponType(2)
                .discountRate(new BigDecimal("0.85")).build();

        when(skuServiceImpl.getById(10L)).thenReturn(sku);
        when(userServiceImpl.getAddressById(1L)).thenReturn(addr);
        when(skuServiceImpl.calculateMoney(any(), any(), eq(false), isNull()))
                .thenReturn(new BigDecimal[]{new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO});
        when(couponsMapper.getUserCouponsByid(5L, 1L)).thenReturn(coupon);
        when(couponsMapper.getTemplatesById(1L)).thenReturn(template);

        ordersService.createOrder(req, 1L);

        verify(ordersMapper).insertOrders(argThat(o ->
                o.getFinalPrice().compareTo(new BigDecimal("85.00")) == 0));
    }

    @Test
    void createOrder_CouponExpired_ThrowsBizException() {
        CreateOrderRequest req = createReq(10L, 1L, LocalDateTime.now().plusHours(2));
        req.setCouponId(5L);
        ServiceSkus sku = sku(10L, 1, new BigDecimal("100"));
        UserAddresses addr = address(1L);
        UserCoupons coupon = UserCoupons.builder().id(5L).templateId(1L).userId(1L)
                .status(CouponStatus.UNUSED.getCode()).expireTime(LocalDateTime.now().minusDays(1)).build();

        when(skuServiceImpl.getById(10L)).thenReturn(sku);
        when(userServiceImpl.getAddressById(1L)).thenReturn(addr);
        when(skuServiceImpl.calculateMoney(any(), any(), eq(false), isNull()))
                .thenReturn(new BigDecimal[]{new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO});
        when(couponsMapper.getUserCouponsByid(5L, 1L)).thenReturn(coupon);

        BizException ex = assertThrows(BizException.class, () -> ordersService.createOrder(req, 1L));
        assertEquals(ErrorConstant.COUPONS_INVALIDED, ex.getMessage());
    }

    @Test
    void createOrder_CouponBelowThreshold_ThrowsBizException() {
        CreateOrderRequest req = createReq(10L, 1L, LocalDateTime.now().plusHours(2));
        req.setCouponId(5L);
        ServiceSkus sku = sku(10L, 1, new BigDecimal("50"));
        UserAddresses addr = address(1L);
        UserCoupons coupon = UserCoupons.builder().id(5L).templateId(1L).userId(1L)
                .status(CouponStatus.UNUSED.getCode()).expireTime(LocalDateTime.now().plusDays(1)).build();
        CouponTemplates template = CouponTemplates.builder().id(1L).couponType(1)
                .thresholdAmount(new BigDecimal("100")).discountAmount(new BigDecimal("20")).build();

        when(skuServiceImpl.getById(10L)).thenReturn(sku);
        when(userServiceImpl.getAddressById(1L)).thenReturn(addr);
        when(skuServiceImpl.calculateMoney(any(), any(), eq(false), isNull()))
                .thenReturn(new BigDecimal[]{new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO});
        when(couponsMapper.getUserCouponsByid(5L, 1L)).thenReturn(coupon);
        when(couponsMapper.getTemplatesById(1L)).thenReturn(template);

        BizException ex = assertThrows(BizException.class, () -> ordersService.createOrder(req, 1L));
        assertEquals(ErrorConstant.COUPONS_NOT_ATTACH_THRESHOLD, ex.getMessage());
    }

    @Test
    void createOrder_WithUrgent_Adds20Percent() {
        CreateOrderRequest req = createReq(10L, 1L, LocalDateTime.now().plusHours(2));
        req.setIsUrgent(1);
        ServiceSkus sku = sku(10L, 1, new BigDecimal("100"));
        UserAddresses addr = address(1L);
        when(skuServiceImpl.getById(10L)).thenReturn(sku);
        when(userServiceImpl.getAddressById(1L)).thenReturn(addr);
        when(skuServiceImpl.calculateMoney(any(), any(), eq(false), isNull()))
                .thenReturn(new BigDecimal[]{new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO});

        ordersService.createOrder(req, 1L);

        verify(ordersMapper).insertOrders(argThat(o ->
                o.getFinalPrice().compareTo(new BigDecimal("120.0")) == 0 &&
                o.getUrgentFee().compareTo(new BigDecimal("20.0")) == 0));
    }

    // ==================== getMyOrders ====================

    @Test
    void getMyOrders_ReturnsList() {
        List<Orders> expected = List.of(new Orders());
        when(ordersMapper.getMyOrders(eq(0), eq(1L), any())).thenReturn(expected);

        List<Orders> result = ordersService.getMyOrders(0, 1L, 1, 10);

        assertEquals(expected, result);
    }

    // ==================== getOrderDetail ====================

    @Test
    void getOrderDetail_Owner_ReturnsDetail() {
        Orders order = Orders.builder().id(1L).userId(1L).build();
        List<OrderEvents> events = List.of(new OrderEvents());
        when(ordersMapper.getOrderById(1L)).thenReturn(order);
        when(ordersMapper.getOrderEventByOrderId(1L)).thenReturn(events);

        var result = ordersService.getOrderDetail(1L);

        assertNotNull(result);
        assertEquals(order, result.getOrders());
        assertEquals(events, result.getOrderEvents());
    }

    @Test
    void getOrderDetail_NotOwner_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(999L).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.getOrderDetail(1L));
        assertEquals(ErrorConstant.ORDER_AUTH_ERROR, ex.getMessage());
    }

    @Test
    void getOrderDetail_NotFound_ThrowsBizException() {
        when(ordersMapper.getOrderById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> ordersService.getOrderDetail(1L));
        assertEquals(ErrorConstant.ORDER_AUTH_ERROR, ex.getMessage());
    }

    // ==================== getOrderEvent ====================

    @Test
    void getOrderEvent_ReturnsEvents() {
        Orders order = Orders.builder().id(1L).userId(1L).build();
        List<OrderEvents> expected = List.of(new OrderEvents());
        when(ordersMapper.getOrderById(1L)).thenReturn(order);
        when(ordersMapper.getOrderEventByOrderId(1L)).thenReturn(expected);

        List<OrderEvents> result = ordersService.getOrderEvent(1L);

        assertEquals(expected, result);
    }

    // ==================== payOrders ====================

    @Test
    void payOrders_Success() {
        Orders order = Orders.builder().id(1L).userId(1L).orderNo("NO001")
                .categoryId(1).appointmentTime(LocalDateTime.now().plusHours(2))
                .status(OrderStatus.WAITING.getCode()).payStatus(PayStatus.UNPAID.getCode())
                .finalPrice(new BigDecimal("100")).build();
        OrderAddressSnapshots addr = OrderAddressSnapshots.builder()
                .lat(new BigDecimal("30.28")).lng(new BigDecimal("120.15")).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);
        when(ordersMapper.getOrderAddressSnapshotsByOrderId(1L)).thenReturn(addr);

        ordersService.payOrders(1L);

        verify(ordersMapper).updateOrders(argThat(o ->
                o.getPayStatus().equals(PayStatus.PAID.getCode())));
        verify(paymentMapper).insertPaymentRecord(any(PaymentRecords.class));
        verify(ordersMapper).insertOrderEvent(any());
        verify(stringRedisTemplate.opsForValue()).set(anyString(), eq("0"));
    }

    @Test
    void payOrders_NotOwner_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(999L).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.payOrders(1L));
        assertEquals(ErrorConstant.ORDER_AUTH_ERROR, ex.getMessage());
    }

    @Test
    void payOrders_AlreadyPaid_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(1L)
                .status(OrderStatus.WAITING.getCode()).payStatus(PayStatus.PAID.getCode()).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.payOrders(1L));
        assertEquals(ErrorConstant.ORDER_STATUS_ERROR, ex.getMessage());
    }

    // ==================== cancelOrder ====================

    @Test
    void cancelOrder_Unpaid_Success() {
        Orders order = Orders.builder().id(1L).userId(1L)
                .status(OrderStatus.WAITING.getCode()).payStatus(PayStatus.UNPAID.getCode()).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        ordersService.cancelOrder(1L);

        verify(ordersMapper).updateOrders(argThat(o ->
                o.getStatus().equals(OrderStatus.CANCELLED.getCode())));
        verify(ordersMapper).insertOrderEvent(any());
    }

    @Test
    void cancelOrder_Paid_Refunds() {
        Orders order = Orders.builder().id(1L).userId(1L)
                .status(OrderStatus.WAITING.getCode()).payStatus(PayStatus.PAID.getCode()).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        ordersService.cancelOrder(1L);

        verify(ordersMapper).updateOrders(argThat(o ->
                o.getStatus().equals(OrderStatus.CANCELLED.getCode()) &&
                o.getPayStatus().equals(PayStatus.REFUNDED.getCode())));
    }

    @Test
    void cancelOrder_NotOwner_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(999L).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.cancelOrder(1L));
        assertEquals(ErrorConstant.ORDER_AUTH_ERROR, ex.getMessage());
    }

    @Test
    void cancelOrder_WrongStatus_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(1L)
                .status(OrderStatus.SERVING.getCode()).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.cancelOrder(1L));
        assertEquals(ErrorConstant.ORDER_STATUS_ERROR, ex.getMessage());
    }

    // ==================== confirmOrder ====================

    @Test
    void confirmOrder_Success() {
        Orders order = Orders.builder().id(1L).userId(1L).workerId(10L)
                .status(OrderStatus.TO_CONFIRM.getCode()).finalPrice(new BigDecimal("100"))
                .distanceFee(BigDecimal.ZERO).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        ordersService.confirmOrder(1L);

        verify(rocketMQTemplate).sendMessageInTransaction(eq("settle-topic:settle-tag"), any(Message.class), isNull());
    }

    @Test
    void confirmOrder_NotOwner_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(999L).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.confirmOrder(1L));
        assertEquals(ErrorConstant.ORDER_AUTH_ERROR, ex.getMessage());
    }

    @Test
    void confirmOrder_WrongStatus_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(1L)
                .status(OrderStatus.SERVING.getCode()).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.confirmOrder(1L));
        assertEquals(ErrorConstant.ORDER_STATUS_ERROR, ex.getMessage());
    }

    // ==================== refund ====================

    @Test
    void refund_WaitingPaid_Success() {
        Orders order = Orders.builder().id(1L).userId(1L)
                .status(OrderStatus.WAITING.getCode()).payStatus(PayStatus.PAID.getCode()).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        ordersService.refund(1L, 1L);

        verify(ordersMapper).updateOrders(argThat(o ->
                o.getStatus().equals(OrderStatus.REFUNDED.getCode())));
        verify(ordersMapper).insertOrderEvent(any());
    }

    @Test
    void refund_NotOwner_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(999L).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.refund(1L, 1L));
        assertEquals("没有操作权限", ex.getMessage());
    }

    @Test
    void refund_AlreadyGrabbed_ThrowsBizException() {
        Orders order = Orders.builder().id(1L).userId(1L)
                .status(OrderStatus.GRABBED.getCode()).payStatus(PayStatus.PAID.getCode()).build();
        when(ordersMapper.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> ordersService.refund(1L, 1L));
        assertEquals("商家已接单，无法退款", ex.getMessage());
    }

    // ==================== 工具方法 ====================

    private CreateOrderRequest createReq(Long skuId, Long addressId, LocalDateTime appointedTime) {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setSkuId(skuId);
        req.setAddressId(addressId);
        req.setAppointedTime(appointedTime);
        req.setIsUrgent(0);
        return req;
    }

    private ServiceSkus sku(Long id, Integer categoryId, BigDecimal basePrice) {
        ServiceSkus s = new ServiceSkus();
        s.setId(id);
        s.setCategoryId(categoryId);
        s.setBasePrice(basePrice);
        return s;
    }

    private UserAddresses address(Long id) {
        UserAddresses a = new UserAddresses();
        a.setId(id);
        a.setProvince("浙江省");
        a.setCity("杭州市");
        a.setDistrict("西湖区");
        a.setDetail("XX路XX号");
        a.setContactName("张三");
        a.setContactPhone("13800138000");
        a.setLat(new BigDecimal("30.28"));
        a.setLng(new BigDecimal("120.15"));
        return a;
    }
}
