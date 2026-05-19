package com.qw.catalog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.service.ISkuService;
import com.qw.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service-skus")
@Tag(name = "服务sku管理")
public class SkuController {

    @Autowired
    ISkuService skuServiceImpl;

    @Operation(summary = "根据分类查询服务")
    @GetMapping
    public Result getByCategory(Long id) throws JsonProcessingException {
        List<ServiceSkus> skus = skuServiceImpl.getByCategory(id);
        if (skus == null) {
            return Result.fail(RedisConstant.CATEGORIES_SKUS_NOT_EXIST);
        }
        return Result.ok(skus);
    }

    @Operation(summary = "根据id查询服务")
    @GetMapping("/{id}")
    public Result<ServiceSkus> getById(@PathVariable Long id) throws JsonProcessingException {
        ServiceSkus skus = skuServiceImpl.getById(id);
        if (skus == null) {
            return Result.fail(RedisConstant.SKUS_SINGLE_NOT_FIND);
        }
        return Result.ok(skus);
    }


}
