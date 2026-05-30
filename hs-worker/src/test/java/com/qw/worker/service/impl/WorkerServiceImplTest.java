package com.qw.worker.service.impl;

import com.qw.catalog.service.ISkuService;
import com.qw.common.constant.OrderStatus;
import com.qw.common.constant.RocketMQConstant;
import com.qw.common.entity.Notifications;
import com.qw.common.entity.Workers;
import com.qw.common.exception.BizException;
import com.qw.common.mapper.WorkersMapper;
import com.qw.common.utils.UserContext;
import com.qw.order.entity.OrderAddressSnapshots;
import com.qw.order.entity.OrderEvents;
import com.qw.order.entity.Orders;
import com.qw.order.mapper.OrdersMapper;
import com.qw.worker.constant.ErrorConstant;
import com.qw.worker.constant.RedisConstant;
import com.qw.worker.constant.RemarkConstant;
import com.qw.worker.dto.LocationRequest;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceImplTest {

    @Mock WorkersMapper workersMapper;
    @Mock StringRedisTemplate stringRedisTemplate;
    @Mock OrdersMapper ordersMapper;
    @Mock RocketMQTemplate rocketMQTemplate;
    @Mock ISkuService skuService;
    @Mock DefaultRedisScript<Long> grabScript;
    @Mock GeoOperations<String, String> geoOperations;

    @InjectMocks
    WorkerServiceImpl workerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workerService, "script", grabScript);
        UserContext.set(1, 1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== getProfile ====================

    @Test
    void getProfile_Exists_ReturnsWorkerWithoutPassword() {
        Workers w = Workers.builder().id(1L).name("张三").passwordHash("secret").build();
        when(workersMapper.selectById(1L)).thenReturn(w);

        Workers result = workerService.getProfile();

        assertNotNull(result);
        assertEquals("张三", result.getName());
        assertNull(result.getPasswordHash());
    }

    @Test
    void getProfile_NotExists_ThrowsBizException() {
        when(workersMapper.selectById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> workerService.getProfile());
        assertEquals(ErrorConstant.NOT_FIND_ERROR, ex.getMessage());
    }

    // ==================== updateOnlineStatus ====================

    @Test
    void updateOnlineStatus_ValidStatus_Success() {
        workerService.updateOnlineStatus(1);

        verify(workersMapper).updateOnlineStatus(1, 1L);
    }

    @Test
    void updateOnlineStatus_InvalidStatus_ThrowsBizException() {
        BizException ex = assertThrows(BizException.class, () -> workerService.updateOnlineStatus(2));
        assertEquals(ErrorConstant.STATUS_ERROR, ex.getMessage());
    }

    @Test
    void updateOnlineStatus_NegativeStatus_ThrowsBizException() {
        BizException ex = assertThrows(BizException.class, () -> workerService.updateOnlineStatus(-1));
        assertEquals(ErrorConstant.STATUS_ERROR, ex.getMessage());
    }

    // ==================== updateLocation ====================

    @Test
    void updateLocation_Success() {
        LocationRequest req = new LocationRequest();
        req.setLng(new BigDecimal("120.15"));
        req.setLat(new BigDecimal("30.28"));
        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);

        workerService.updateLocation(req);

        verify(geoOperations).add(anyString(), any(), anyString());
        verify(workersMapper).updateWorkerLocation(eq(1L), eq(new BigDecimal("120.15")), eq(new BigDecimal("30.28")));
    }

    // ==================== grabOrder ====================

    @Test
    void grabOrder_RateLimited_ThrowsBizException() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                .thenReturn(-2L);

        BizException ex = assertThrows(BizException.class, () -> workerService.grabOrder(100L));
        assertTrue(ex.getMessage().contains("请求过于频繁"));
    }

    @Test
    void grabOrder_OrderNotInPool_ThrowsBizException() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                .thenReturn(-1L);

        BizException ex = assertThrows(BizException.class, () -> workerService.grabOrder(100L));
        assertEquals(ErrorConstant.ORDER_NOT_EXISTS, ex.getMessage());
    }

    @Test
    void grabOrder_AlreadyGrabbed_ThrowsBizException() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                .thenReturn(0L);

        BizException ex = assertThrows(BizException.class, () -> workerService.grabOrder(100L));
        assertEquals(ErrorConstant.ORDER_HAVE_BE_GRAB, ex.getMessage());
    }

    @Test
    void grabOrder_Success_WithDistanceFee() {
        Orders grabbed = Orders.builder().id(100L).orderNo("NO123").userId(2L)
                .appointmentTime(LocalDateTime.now().plusHours(2))
                .basePrice(new BigDecimal("100")).status(OrderStatus.WAITING.getCode()).build();
        Workers worker = Workers.builder().id(1L).lastLat(new BigDecimal("30.29"))
                .lastLng(new BigDecimal("120.16")).build();
        OrderAddressSnapshots address = OrderAddressSnapshots.builder()
                .lat(new BigDecimal("30.20")).lng(new BigDecimal("120.10")).build();

        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                .thenReturn(1L);
        when(ordersMapper.getOrderById(100L)).thenReturn(grabbed);
        when(workersMapper.selectById(1L)).thenReturn(worker);
        when(ordersMapper.getOrderAddressSnapshotsByOrderId(100L)).thenReturn(address);
        when(skuService.calculateMoney(any(), any(), eq(true), anyDouble()))
                .thenReturn(new BigDecimal[]{new BigDecimal("120"), new BigDecimal("10"), new BigDecimal("10")});

        Long result = workerService.grabOrder(100L);

        assertEquals(1L, result);
        verify(ordersMapper).updateOrders(argThat(o ->
                o.getStatus().equals(OrderStatus.GRABBED.getCode()) &&
                o.getWorkerId().equals(1L)));
        verify(ordersMapper).insertOrderEvent(any());
        verify(stringRedisTemplate).delete(RedisConstant.ORDER_GRAB_PREFIX + 100L);
        verify(rocketMQTemplate).syncSend(eq(RocketMQConstant.NOTIFICATION_TOPIC), any(Notifications.class));
    }

    @Test
    void grabOrder_Success_WorkerNoCoordinates_NoDistanceFee() {
        Orders grabbed = Orders.builder().id(100L).orderNo("NO123").userId(2L)
                .appointmentTime(LocalDateTime.now().plusHours(2))
                .basePrice(new BigDecimal("100")).status(OrderStatus.WAITING.getCode()).build();
        Workers worker = Workers.builder().id(1L).lastLat(null).lastLng(null).build();

        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                .thenReturn(1L);
        when(ordersMapper.getOrderById(100L)).thenReturn(grabbed);
        when(workersMapper.selectById(1L)).thenReturn(worker);
        when(ordersMapper.getOrderAddressSnapshotsByOrderId(100L)).thenReturn(null);

        workerService.grabOrder(100L);

        verify(ordersMapper).updateOrders(argThat(o ->
                o.getDistanceFee().compareTo(BigDecimal.ZERO) == 0));
    }

    // ==================== getMyOrder ====================

    @Test
    void getMyOrder_ReturnsList() {
        List<Orders> expected = List.of(new Orders());
        when(ordersMapper.getOrdersByWorkerId(1L)).thenReturn(expected);

        List<Orders> result = workerService.getMyOrder();

        assertEquals(expected, result);
    }

    // ==================== startService ====================

    @Test
    void startService_Success() {
        Orders order = Orders.builder().id(200L).workerId(1L)
                .status(OrderStatus.GRABBED.getCode()).build();
        when(ordersMapper.getOrderById(200L)).thenReturn(order);

        workerService.startService(200L);

        verify(ordersMapper).updateOrders(argThat(o ->
                o.getStatus().equals(OrderStatus.SERVING.getCode())));
        verify(ordersMapper).insertOrderEvent(any(OrderEvents.class));
    }

    @Test
    void startService_NotWorkerOwner_ThrowsBizException() {
        Orders order = Orders.builder().id(200L).workerId(999L).build();
        when(ordersMapper.getOrderById(200L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> workerService.startService(200L));
        assertEquals(ErrorConstant.AUTH_NOT_ENOUGH, ex.getMessage());
    }

    @Test
    void startService_WrongStatus_ThrowsBizException() {
        Orders order = Orders.builder().id(200L).workerId(1L)
                .status(OrderStatus.SERVING.getCode()).build();
        when(ordersMapper.getOrderById(200L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> workerService.startService(200L));
        assertEquals(ErrorConstant.STATUS_ERROR, ex.getMessage());
    }

    // ==================== completeService ====================

    @Test
    void completeService_Success() {
        Orders order = Orders.builder().id(200L).workerId(1L).userId(2L)
                .orderNo("NO456").status(OrderStatus.SERVING.getCode()).build();
        when(ordersMapper.getOrderById(200L)).thenReturn(order);

        workerService.completeService(200L);

        verify(ordersMapper).updateOrders(argThat(o ->
                o.getStatus().equals(OrderStatus.TO_CONFIRM.getCode())));
        verify(ordersMapper).insertOrderEvent(any(OrderEvents.class));
        verify(rocketMQTemplate).syncSend(eq(RocketMQConstant.NOTIFICATION_TOPIC), any(Notifications.class));
    }

    @Test
    void completeService_NotWorkerOwner_ThrowsBizException() {
        Orders order = Orders.builder().id(200L).workerId(999L).build();
        when(ordersMapper.getOrderById(200L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> workerService.completeService(200L));
        assertEquals(ErrorConstant.AUTH_NOT_ENOUGH, ex.getMessage());
    }

    @Test
    void completeService_WrongStatus_ThrowsBizException() {
        Orders order = Orders.builder().id(200L).workerId(1L)
                .status(OrderStatus.WAITING.getCode()).build();
        when(ordersMapper.getOrderById(200L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> workerService.completeService(200L));
        assertEquals(ErrorConstant.STATUS_ERROR, ex.getMessage());
    }
}
