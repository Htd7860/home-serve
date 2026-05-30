package com.qw.admin.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.admin.dto.CouponTemplatesRequest;
import com.qw.admin.dto.PricingRuleRequest;
import com.qw.admin.dto.SeckilActivitiesRequest;
import com.qw.admin.dto.ServiceCategoryRequest;
import com.qw.admin.dto.SkuServiceRequest;
import com.qw.catalog.constant.RedisConstant;
import com.qw.common.service.FileService;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.mapper.CategoriesMapper;
import com.qw.catalog.mapper.PricingRuleMapper;
import com.qw.catalog.mapper.SkuMapper;
import com.qw.common.constant.SeckillActivityStatus;
import com.qw.common.entity.Workers;
import com.qw.common.exception.BizException;
import com.qw.common.mapper.WorkersMapper;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.SeckillActivities;
import com.qw.marketing.mapper.CouponsMapper;
import com.qw.marketing.mapper.SeckillMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock CategoriesMapper categoriesMapper;
    @Mock StringRedisTemplate stringRedisTemplate;
    @Mock Cache<String, Object> categoriesCache;
    @Mock Cache<String, Object> skuCache;
    @Mock Cache<String, Object> pricingCache;
    @Mock SkuMapper skuMapper;
    @Mock WorkersMapper workersMapper;
    @Mock SeckillMapper seckillMapper;
    @Mock CouponsMapper couponsMapper;
    @Mock PricingRuleMapper pricingRuleMapper;
    @Mock RBloomFilter<Object> skuBloomFilter;
    @Mock RBloomFilter<Object> categoryBloomFilter;
    @Mock FileService fileService;

    @InjectMocks
    AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        // AdminServiceImpl 中 RBloomFilter 是 raw type，InjectMocks 同名注入会失效，手动塞
        ReflectionTestUtils.setField(adminService, "skuBloomFilter", skuBloomFilter);
        ReflectionTestUtils.setField(adminService, "categoryBloomFilter", categoryBloomFilter);
    }

    // ==================== 分类管理 ====================

    @Test
    void insertWithOne_Success() {
        ServiceCategoryRequest req = ServiceCategoryRequest.builder().name("家电清洗").sortOrder(1).build();
        when(categoriesMapper.insertWithOne(any())).thenReturn(1);

        adminService.insertWithOne(req);

        verify(stringRedisTemplate).delete(RedisConstant.ALL_CATEGORIES);
        verify(categoriesCache).invalidate(RedisConstant.ALL_CATEGORIES);
    }

    @Test
    void insertWithOne_Fail_ThrowsException() {
        when(categoriesMapper.insertWithOne(any())).thenReturn(0);

        assertThrows(BizException.class, () ->
                adminService.insertWithOne(ServiceCategoryRequest.builder().name("x").build()));
    }

    @Test
    void updateCategoryByid_Success() {
        ServiceCategoryRequest req = ServiceCategoryRequest.builder().name("改名").sortOrder(2).build();
        when(categoriesMapper.updateCategoryById(any())).thenReturn(1);

        adminService.updateCategoryByid(1, req);

        verify(stringRedisTemplate).delete(RedisConstant.CATEGORIES_PREFIX + 1);
        verify(stringRedisTemplate).delete(RedisConstant.ALL_CATEGORIES);
        verify(categoriesCache).invalidate(RedisConstant.ALL_CATEGORIES);
        verify(categoriesCache).invalidate(RedisConstant.CATEGORIES_PREFIX + 1);
    }

    @Test
    void updateCategoryByid_NotFound_ThrowsException() {
        when(categoriesMapper.updateCategoryById(any())).thenReturn(0);

        assertThrows(BizException.class, () ->
                adminService.updateCategoryByid(99, ServiceCategoryRequest.builder().name("x").build()));
    }

    @Test
    void deleteCategoryById_Success() {
        when(skuMapper.getByCategory(1)).thenReturn(null);
        when(categoriesMapper.deleteById(1)).thenReturn(1);

        adminService.deleteCategoryById(1);

        verify(stringRedisTemplate).delete(RedisConstant.CATEGORIES_PREFIX + 1);
        verify(stringRedisTemplate).delete(RedisConstant.ALL_CATEGORIES);
    }

    @Test
    void deleteCategoryById_HasSkus_ThrowsException() {
        when(skuMapper.getByCategory(1)).thenReturn(List.of(new ServiceSkus()));

        assertThrows(BizException.class, () -> adminService.deleteCategoryById(1));
        verify(categoriesMapper, never()).deleteById(anyInt());
    }

    @Test
    void deleteCategoryById_NotFound_ThrowsException() {
        when(skuMapper.getByCategory(1)).thenReturn(null);
        when(categoriesMapper.deleteById(1)).thenReturn(0);

        assertThrows(BizException.class, () -> adminService.deleteCategoryById(1));
    }

    // ==================== SKU 管理 ====================

    @Test
    void addSku_WithCoverImage_Success() {
        SkuServiceRequest req = SkuServiceRequest.builder()
                .categoryId(1).name("空调清洗").description("深度清洗")
                .basePrice(new BigDecimal("199")).durationMinutes(60).unit("台")
                .status(1).coverImage("https://img.jpg").build();
        when(skuMapper.insertWithImg(any())).thenReturn(1);

        adminService.addSku(req);

        verify(stringRedisTemplate).delete(RedisConstant.SKUS_PREFIX + 1);
        verify(skuCache).invalidate(RedisConstant.SKUS_PREFIX + 1);
    }

    @Test
    void addSku_NoCoverImage_Success() {
        SkuServiceRequest req = SkuServiceRequest.builder()
                .categoryId(2).name("维修").description("上门维修")
                .basePrice(new BigDecimal("50")).durationMinutes(30).unit("次")
                .status(1).coverImage(null).build();
        when(skuMapper.insertWithNoImg(any())).thenReturn(1);

        adminService.addSku(req);

        verify(skuMapper).insertWithNoImg(any());
    }

    @Test
    void addSku_Fail_ThrowsException() {
        SkuServiceRequest req = SkuServiceRequest.builder()
                .categoryId(1).name("x").description("x")
                .basePrice(BigDecimal.ONE).durationMinutes(10).unit("次")
                .status(1).build();
        when(skuMapper.insertWithNoImg(any())).thenReturn(0);

        assertThrows(BizException.class, () -> adminService.addSku(req));
    }

    @Test
    void updateSku_Success() {
        SkuServiceRequest req = SkuServiceRequest.builder()
                .categoryId(1).name("改名").description("新描述")
                .basePrice(new BigDecimal("299")).durationMinutes(90).unit("台")
                .status(1).build();
        when(skuMapper.updateSku(any())).thenReturn(1);

        adminService.updateSku(10L, req);

        verify(stringRedisTemplate).delete(RedisConstant.SKUS_PREFIX + 1);
        verify(skuCache).invalidate(RedisConstant.SKUS_PREFIX + 1);
        verify(stringRedisTemplate).delete(RedisConstant.SKUS_SINGLE_PREFIX + 10L);
        verify(skuCache).invalidate(RedisConstant.SKUS_SINGLE_PREFIX + 10L);
    }

    @Test
    void updateSku_Fail_ThrowsException() {
        when(skuMapper.updateSku(any())).thenReturn(0);
        SkuServiceRequest req = SkuServiceRequest.builder().categoryId(1).name("x").description("x")
                .basePrice(BigDecimal.ONE).durationMinutes(10).unit("次").status(1).build();

        assertThrows(BizException.class, () -> adminService.updateSku(99L, req));
    }

    // ==================== 服务者审核 ====================

    @Test
    void getPendingList_ReturnsList() {
        List<Workers> list = List.of(new Workers(), new Workers());
        when(workersMapper.getPendingWorkers()).thenReturn(list);

        assertEquals(2, adminService.getPendingList().size());
    }

    @Test
    void approveById_Success() {
        when(workersMapper.approveById(1L)).thenReturn(1);
        adminService.approveById(1L);
        verify(workersMapper).approveById(1L);
    }

    @Test
    void approveById_Fail_ThrowsException() {
        when(workersMapper.approveById(1L)).thenReturn(0);
        assertThrows(BizException.class, () -> adminService.approveById(1L));
    }

    @Test
    void rejectById_Success() {
        when(workersMapper.rejectById(1L)).thenReturn(1);
        adminService.rejectById(1L);
        verify(workersMapper).rejectById(1L);
    }

    @Test
    void rejectById_Fail_ThrowsException() {
        when(workersMapper.rejectById(1L)).thenReturn(0);
        assertThrows(BizException.class, () -> adminService.rejectById(1L));
    }

    // ==================== 秒杀活动 ====================

    @Test
    void addActivities_Success() {
        SeckilActivitiesRequest req = SeckilActivitiesRequest.builder()
                .activityName("618大促").totalStock(100)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(3))
                .templateId(1L).categoryId(1L).build();
        when(seckillMapper.insert(any())).thenReturn(1);

        adminService.addActivities(req);

        verify(seckillMapper).insert(any());
    }

    @Test
    void addActivities_StartTooSoon_ThrowsException() {
        SeckilActivitiesRequest req = SeckilActivitiesRequest.builder()
                .startTime(LocalDateTime.now().plusMinutes(5))
                .endTime(LocalDateTime.now().plusHours(3)).build();

        assertThrows(BizException.class, () -> adminService.addActivities(req));
    }

    @Test
    void addActivities_StartAfterEnd_ThrowsException() {
        SeckilActivitiesRequest req = SeckilActivitiesRequest.builder()
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(1)).build();

        assertThrows(BizException.class, () -> adminService.addActivities(req));
    }

    @Test
    void addActivities_Fail_ThrowsException() {
        SeckilActivitiesRequest req = SeckilActivitiesRequest.builder()
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(3)).build();
        when(seckillMapper.insert(any())).thenReturn(0);

        assertThrows(BizException.class, () -> adminService.addActivities(req));
    }

    @Test
    void updateActivities_Draft_Success() {
        SeckilActivitiesRequest req = SeckilActivitiesRequest.builder()
                .activityName("改名").totalStock(200)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .templateId(1L).categoryId(1L).build();
        SeckillActivities existing = SeckillActivities.builder()
                .id(1L).status(SeckillActivityStatus.DRAFT.getCode()).build();
        when(seckillMapper.getById(1L)).thenReturn(existing);

        adminService.updateActivities(1L, req);

        verify(seckillMapper).update(any());
        verify(stringRedisTemplate).delete(com.qw.marketing.constant.RedisConstant.SECKILL_ACTIVITIES);
    }

    @Test
    void updateActivities_Preheat_UpdatesStockAndEndTime() {
        SeckilActivitiesRequest req = SeckilActivitiesRequest.builder()
                .totalStock(300).endTime(LocalDateTime.now().plusHours(5)).build();
        SeckillActivities existing = SeckillActivities.builder()
                .id(1L).status(SeckillActivityStatus.PREHEAT.getCode()).build();
        when(seckillMapper.getById(1L)).thenReturn(existing);

        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("seckill:stock:1")).thenReturn("100");

        adminService.updateActivities(1L, req);

        verify(ops).increment("seckill:stock:1", 200L);
        verify(seckillMapper).update(any());
    }

    @Test
    void updateActivities_InProgress_OnlyUpdatesEndTime() {
        SeckilActivitiesRequest req = SeckilActivitiesRequest.builder()
                .totalStock(500).endTime(LocalDateTime.now().plusHours(6)).build();
        SeckillActivities existing = SeckillActivities.builder()
                .id(1L).status(SeckillActivityStatus.IN_PROGRESS.getCode()).build();
        when(seckillMapper.getById(1L)).thenReturn(existing);

        adminService.updateActivities(1L, req);

        verify(seckillMapper).update(any());
    }

    @Test
    void updateActivities_Ended_ThrowsException() {
        SeckillActivities existing = SeckillActivities.builder()
                .id(1L).status(SeckillActivityStatus.ENDED.getCode()).build();
        when(seckillMapper.getById(1L)).thenReturn(existing);

        assertThrows(BizException.class, () ->
                adminService.updateActivities(1L, SeckilActivitiesRequest.builder().build()));
    }

    @Test
    void updateActivities_NotFound_ThrowsException() {
        when(seckillMapper.getById(1L)).thenReturn(null);

        assertThrows(BizException.class, () ->
                adminService.updateActivities(1L, SeckilActivitiesRequest.builder().build()));
    }

    @Test
    void getAllActivities_ReturnsList() {
        List<SeckillActivities> list = List.of(new SeckillActivities(), new SeckillActivities());
        when(seckillMapper.listAll()).thenReturn(list);

        assertEquals(2, adminService.getAllActivities().size());
    }

    @Test
    void deleteActivity_Draft_Success() {
        SeckillActivities activity = SeckillActivities.builder()
                .id(1L).status(SeckillActivityStatus.DRAFT.getCode()).build();
        when(seckillMapper.getById(1L)).thenReturn(activity);
        when(seckillMapper.delete(1L)).thenReturn(1);

        adminService.deleteActivity(1L);

        verify(seckillMapper).delete(1L);
        verify(stringRedisTemplate).delete(com.qw.marketing.constant.RedisConstant.SECKILL_ACTIVITIES);
    }

    @Test
    void deleteActivity_NotDraft_ThrowsException() {
        SeckillActivities activity = SeckillActivities.builder()
                .id(1L).status(SeckillActivityStatus.IN_PROGRESS.getCode()).build();
        when(seckillMapper.getById(1L)).thenReturn(activity);

        assertThrows(BizException.class, () -> adminService.deleteActivity(1L));
        verify(seckillMapper, never()).delete(any());
    }

    @Test
    void deleteActivity_NotFound_ThrowsException() {
        when(seckillMapper.getById(1L)).thenReturn(null);

        assertThrows(BizException.class, () -> adminService.deleteActivity(1L));
    }

    // ==================== 优惠券模板 ====================

    @Test
    void getAllTemplates_ReturnsList() {
        List<CouponTemplates> list = List.of(new CouponTemplates(), new CouponTemplates());
        when(couponsMapper.listAllTemplates()).thenReturn(list);

        assertEquals(2, adminService.getAllTemplates().size());
    }

    @Test
    void addTemplate_Success() {
        CouponTemplatesRequest req = CouponTemplatesRequest.builder()
                .couponName("新人券").couponType(1).discountAmount(new BigDecimal("10"))
                .validDays(30).type(1).status(1).build();
        when(couponsMapper.insertTemplate(any())).thenReturn(1);

        adminService.addTemplate(req);

        verify(couponsMapper).insertTemplate(any());
    }

    @Test
    void addTemplate_Fail_ThrowsException() {
        when(couponsMapper.insertTemplate(any())).thenReturn(0);

        assertThrows(BizException.class, () ->
                adminService.addTemplate(CouponTemplatesRequest.builder()
                        .couponName("x").couponType(1).validDays(7).type(1).status(1).build()));
    }

    @Test
    void updateTemplate_Success() {
        CouponTemplatesRequest req = CouponTemplatesRequest.builder()
                .couponName("改名券").couponType(1).discountAmount(new BigDecimal("20"))
                .validDays(15).type(1).status(1).build();
        when(couponsMapper.getTemplatesById(1L)).thenReturn(new CouponTemplates());
        when(couponsMapper.updateTemplate(any())).thenReturn(1);

        adminService.updateTemplate(1L, req);

        verify(couponsMapper).updateTemplate(any());
    }

    @Test
    void updateTemplate_NotFound_ThrowsException() {
        when(couponsMapper.getTemplatesById(1L)).thenReturn(null);

        assertThrows(BizException.class, () ->
                adminService.updateTemplate(1L, CouponTemplatesRequest.builder()
                        .couponName("x").couponType(1).validDays(7).type(1).status(1).build()));
    }

    @Test
    void updateTemplate_Fail_ThrowsException() {
        when(couponsMapper.getTemplatesById(1L)).thenReturn(new CouponTemplates());
        when(couponsMapper.updateTemplate(any())).thenReturn(0);

        assertThrows(BizException.class, () ->
                adminService.updateTemplate(1L, CouponTemplatesRequest.builder()
                        .couponName("x").couponType(1).validDays(7).type(1).status(1).build()));
    }

    // ==================== 定价规则 ====================

    @Test
    void getAllPricingRules_ReturnsList() {
        when(pricingRuleMapper.listAll()).thenReturn(List.of());

        assertNotNull(adminService.getAllPricingRules());
    }

    @Test
    void addPricingRule_Success() {
        PricingRuleRequest req = PricingRuleRequest.builder()
                .ruleType("DISCOUNT").ruleName("VIP折扣").ruleConfig("{\"rate\":0.8}")
                .priority(1).status(1).build();
        when(pricingRuleMapper.insert(any())).thenReturn(1);

        adminService.addPricingRule(req);

        verify(pricingRuleMapper).insert(any());
        verify(pricingCache).invalidate(RedisConstant.PRICING_RULE_KEY);
        verify(stringRedisTemplate).delete(RedisConstant.PRICING_RULE_KEY);
    }

    @Test
    void addPricingRule_Fail_ThrowsException() {
        when(pricingRuleMapper.insert(any())).thenReturn(0);

        assertThrows(BizException.class, () ->
                adminService.addPricingRule(PricingRuleRequest.builder()
                        .ruleType("x").ruleName("x").ruleConfig("{}").priority(1).status(1).build()));
    }

    @Test
    void updatePricingRule_Success() {
        PricingRuleRequest req = PricingRuleRequest.builder()
                .ruleType("DISCOUNT").ruleName("新折扣").ruleConfig("{\"rate\":0.9}")
                .priority(2).status(1).build();
        when(pricingRuleMapper.update(any())).thenReturn(1);

        adminService.updatePricingRule(1, req);

        verify(pricingRuleMapper).update(any());
        verify(pricingCache).invalidate(RedisConstant.PRICING_RULE_KEY);
        verify(stringRedisTemplate).delete(RedisConstant.PRICING_RULE_KEY);
    }

    @Test
    void updatePricingRule_Fail_ThrowsException() {
        when(pricingRuleMapper.update(any())).thenReturn(0);

        assertThrows(BizException.class, () ->
                adminService.updatePricingRule(1, PricingRuleRequest.builder()
                        .ruleType("x").ruleName("x").ruleConfig("{}").priority(1).status(1).build()));
    }

    @Test
    void deletePricingRule_Success() {
        when(pricingRuleMapper.delete(1)).thenReturn(1);

        adminService.deletePricingRule(1);

        verify(pricingCache).invalidate(RedisConstant.PRICING_RULE_KEY);
        verify(stringRedisTemplate).delete(RedisConstant.PRICING_RULE_KEY);
    }

    @Test
    void deletePricingRule_Fail_ThrowsException() {
        when(pricingRuleMapper.delete(1)).thenReturn(0);

        assertThrows(BizException.class, () -> adminService.deletePricingRule(1));
    }

    // ==================== 图片上传 ====================

    @Test
    void uploadImg_ValidFile_ReturnsUrl() {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image".getBytes());
        when(fileService.upload(any())).thenReturn("https://example.com/abc123.jpg");

        String url = adminService.uploadImg(file);

        assertEquals("https://example.com/abc123.jpg", url);
        verify(fileService).upload(file);
    }

    @Test
    void uploadImg_PropagatesBizException() {
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "fake".getBytes());
        when(fileService.upload(any())).thenThrow(new BizException("不支持的文件类型"));

        BizException ex = assertThrows(BizException.class, () -> adminService.uploadImg(file));
        assertEquals("不支持的文件类型", ex.getMessage());
    }
}
