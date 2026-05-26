package com.qw.admin.controller;

import com.qw.admin.dto.ServiceCategoryRequest;
import com.qw.admin.dto.SkuServiceRequest;
import com.qw.admin.service.IAdminService;
import com.qw.admin.validator.OnCreate;
import com.qw.admin.validator.OnUpdate;
import com.qw.catalog.service.ICategoriesService;
import com.qw.common.result.Result;
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
    @GetMapping
    @Operation(summary = "获得所有分类")
    public Result getServiceCategory(){
        return Result.ok(categoriesServiceImpl.list());
    }

    @PostMapping
    @Operation(summary = "新增分类")
    public Result addCategory(@Valid @RequestBody ServiceCategoryRequest req){
        adminServiceImpl.insertWithOne(req);
        return Result.ok();
    }

    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    public Result uploadImage(@RequestParam("file") MultipartFile file){
        return Result.ok();
    }

    @PutMapping("/service-categories/{id}")
    @Operation(summary = "修改分类")
    public Result updateCategoryById(@PathVariable Integer id,@RequestBody ServiceCategoryRequest req){
        adminServiceImpl.updateCategoryByid(id,req);
        return Result.ok();
    }

    @DeleteMapping("/service-categories/{id}")
    @Operation(summary = "删除分类")
    public Result deleteCategoryById(@PathVariable Integer id){
        adminServiceImpl.deleteCategoryById(id);
        return Result.ok();
    }

    @PostMapping("/service-skus")
    @Operation(summary = "新增sku")
    public Result addSkuService(@Validated(OnCreate.class) @RequestBody SkuServiceRequest req){
        adminServiceImpl.addSku(req);
        return Result.ok();
    }

    @PutMapping("/service-skus/{id}")
    @Operation(summary = "修改sku")
    public Result updateSkuService(@PathVariable Long id,@Validated(OnUpdate.class) @RequestBody SkuServiceRequest req){
        adminServiceImpl.updateSku(id,req);
        return Result.ok();
    }

    @GetMapping("/workers/pending")
    @Operation(summary = "查看审核列表")
    public Result getPendingList(){
        return Result.ok(adminServiceImpl.getPendingList());
    }

    @PutMapping("/workers/{id}/approve")
    @Operation(summary = "通过申请")
    public Result approveWorker(@PathVariable Long id){
        adminServiceImpl.approveById(id);
        return Result.ok();
    }

    @PutMapping("/workers/{id}/reject")
    @Operation(summary = "拒绝申请")
    public Result rejectWorker(@PathVariable Long id){
        adminServiceImpl.rejectById(id);
        return Result.ok();
    }
}
