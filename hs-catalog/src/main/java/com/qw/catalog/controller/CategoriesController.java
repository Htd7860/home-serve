package com.qw.catalog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.ServiceCategories;
import com.qw.catalog.service.ICategoriesService;
import com.qw.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name = "分类管理")
public class CategoriesController {

    @Autowired
    ICategoriesService categoriesServiceImpl;

    @GetMapping
    public Result<List<ServiceCategories>> list() throws JsonProcessingException {
        List<ServiceCategories> list = categoriesServiceImpl.list();
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) throws JsonProcessingException {
        ServiceCategories categories = categoriesServiceImpl.getById(id);
        if (categories == null) {
            return Result.fail(RedisConstant.CATEGORIES_NOT_EXIST);
        }
        return Result.ok(categories);
    }
}
