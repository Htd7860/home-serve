package com.qw.admin.service;

import com.qw.admin.dto.ServiceCategoryRequest;
import com.qw.admin.dto.SkuServiceRequest;
import com.qw.common.entity.Workers;

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
}
