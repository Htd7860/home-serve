package com.qw.admin.controller;

import com.qw.admin.dto.CouponTemplatesRequest;
import com.qw.admin.dto.PricingRuleRequest;
import com.qw.admin.dto.SeckilActivitiesRequest;
import com.qw.admin.dto.ServiceCategoryRequest;
import com.qw.admin.dto.SkuServiceRequest;
import com.qw.admin.service.IAdminService;
import com.qw.admin.validator.OnCreate;
import com.qw.admin.validator.OnUpdate;
import com.qw.catalog.service.ICategoriesService;
import com.qw.common.result.Result;
import com.qw.user.annotation.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author：qw
 * @Package：com.qw.admin.controller
 * @Project：home-serve
 * @name：AdminController
 * @Date：2026/5/26 18:40
 * @Filename：AdminController
 */

@RestController
@RequestMapping("/admin")
@Tag(name = "管理员模块")
public class AdminController {
    @Autowired
    ICategoriesService categoriesServiceImpl;
    @Autowired
    IAdminService adminServiceImpl;
    @RequireRole({"3"})
    @GetMapping("/service-categories")
    @Operation(summary = "获得所有分类")
    public Result getServiceCategory(){
        return Result.ok(categoriesServiceImpl.list());
    }

    @RequireRole({"3"})
    @PostMapping("/service-categories")
    @Operation(summary = "新增分类")
    public Result addCategory(@Valid @RequestBody ServiceCategoryRequest req){
        adminServiceImpl.insertWithOne(req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    public Result uploadImage(@RequestParam("file") MultipartFile file){
        return Result.ok();
    }

    @RequireRole({"3"})
    @PutMapping("/service-categories/{id}")
    @Operation(summary = "修改分类")
    public Result updateCategoryById(@PathVariable Integer id,@RequestBody ServiceCategoryRequest req){
        adminServiceImpl.updateCategoryByid(id,req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @DeleteMapping("/service-categories/{id}")
    @Operation(summary = "删除分类")
    public Result deleteCategoryById(@PathVariable Integer id){
        adminServiceImpl.deleteCategoryById(id);
        return Result.ok();
    }

    @RequireRole({"3"})
    @PostMapping("/service-skus")
    @Operation(summary = "新增sku")
    public Result addSkuService(@Validated(OnCreate.class) @RequestBody SkuServiceRequest req){
        adminServiceImpl.addSku(req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @PutMapping("/service-skus/{id}")
    @Operation(summary = "修改sku")
    public Result updateSkuService(@PathVariable Long id,@Validated(OnUpdate.class) @RequestBody SkuServiceRequest req){
        adminServiceImpl.updateSku(id,req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @GetMapping("/workers/pending")
    @Operation(summary = "查看审核列表")
    public Result getPendingList(){
        return Result.ok(adminServiceImpl.getPendingList());
    }

    @RequireRole({"3"})
    @PutMapping("/workers/{id}/approve")
    @Operation(summary = "通过申请")
    public Result approveWorker(@PathVariable Long id){
        adminServiceImpl.approveById(id);
        return Result.ok();
    }

    @RequireRole({"3"})
    @PutMapping("/workers/{id}/reject")
    @Operation(summary = "拒绝申请")
    public Result rejectWorker(@PathVariable Long id){
        adminServiceImpl.rejectById(id);
        return Result.ok();
    }
    @RequireRole({"3"})
    @GetMapping("/seckill/activities")
    @Operation(summary = "查看全部秒杀活动")
    public Result getSeckillActivities(){
        return Result.ok(adminServiceImpl.getAllActivities());
    }

    @RequireRole({"3"})
    @PostMapping("/seckill/activities")
    @Operation(summary = "创建新的活动")
    public Result addSeckillActivities(@Validated @RequestBody SeckilActivitiesRequest req){
        adminServiceImpl.addActivities(req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @PutMapping("/seckill/activities/{id}")
    @Operation(summary = "修改活动")
    public Result updateSeckillActivities(@Validated @RequestBody SeckilActivitiesRequest req,@PathVariable Long id){
        adminServiceImpl.updateActivities(id,req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @DeleteMapping("/seckill/activities/{id}")
    @Operation(summary = "删除活动")
    public Result deleteSeckillActivities(@PathVariable Long id){
        adminServiceImpl.deleteActivity(id);
        return Result.ok();
    }

    @RequireRole({"3"})
    @GetMapping("/coupon-templates")
    @Operation(summary = "查看全部优惠券模板")
    public Result getCouponTemplates(){
        return Result.ok(adminServiceImpl.getAllTemplates());
    }

    @RequireRole({"3"})
    @PostMapping("/coupon-templates")
    @Operation(summary = "创建优惠券模板")
    public Result addCouponTemplate(@Validated @RequestBody CouponTemplatesRequest req){
        adminServiceImpl.addTemplate(req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @PutMapping("/coupon-templates/{id}")
    @Operation(summary = "修改优惠券模板")
    public Result updateCouponTemplate(@PathVariable Long id, @Validated @RequestBody CouponTemplatesRequest req){
        adminServiceImpl.updateTemplate(id, req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @GetMapping("/pricing-rules")
    @Operation(summary = "查看全部定价规则")
    public Result getPricingRules(){
        return Result.ok(adminServiceImpl.getAllPricingRules());
    }

    @RequireRole({"3"})
    @PostMapping("/pricing-rules")
    @Operation(summary = "新增定价规则")
    public Result addPricingRule(@Validated @RequestBody PricingRuleRequest req){
        adminServiceImpl.addPricingRule(req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @PutMapping("/pricing-rules/{id}")
    @Operation(summary = "修改定价规则")
    public Result updatePricingRule(@PathVariable Integer id, @Validated @RequestBody PricingRuleRequest req){
        adminServiceImpl.updatePricingRule(id, req);
        return Result.ok();
    }

    @RequireRole({"3"})
    @DeleteMapping("/pricing-rules/{id}")
    @Operation(summary = "删除定价规则")
    public Result deletePricingRule(@PathVariable Integer id){
        adminServiceImpl.deletePricingRule(id);
        return Result.ok();
    }


}
