package com.qw.admin.service;

import com.qw.admin.dto.CouponTemplatesRequest;
import com.qw.admin.dto.PricingRuleRequest;
import com.qw.admin.dto.SeckilActivitiesRequest;
import com.qw.admin.dto.ServiceCategoryRequest;
import com.qw.admin.dto.SkuServiceRequest;
import com.qw.catalog.entity.PricingRules;
import com.qw.common.entity.Workers;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.SeckillActivities;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.admin.service
 * @Project：home-serve
 * @name：IAdminService
 * @Date：2026/5/26 18:42
 * @Filename：IAdminService
 */
public interface IAdminService {
    void insertWithOne(ServiceCategoryRequest req);

    void updateCategoryByid(Integer id,ServiceCategoryRequest req);

    void deleteCategoryById(Integer id);

    void addSku(SkuServiceRequest req);

    void updateSku(Long id, SkuServiceRequest req);

    List<Workers> getPendingList();

    void approveById(Long id);

    void rejectById(Long id);

    void addActivities(SeckilActivitiesRequest req);

    void updateActivities(Long id, SeckilActivitiesRequest req);

    void deleteActivity(Long id);

    List<SeckillActivities> getAllActivities();

    List<CouponTemplates> getAllTemplates();

    void addTemplate(CouponTemplatesRequest req);

    void updateTemplate(Long id, CouponTemplatesRequest req);

    List<PricingRules> getAllPricingRules();

    void addPricingRule(PricingRuleRequest req);

    void updatePricingRule(Integer id, PricingRuleRequest req);

    void deletePricingRule(Integer id);

    String uploadImg(MultipartFile file);
}
