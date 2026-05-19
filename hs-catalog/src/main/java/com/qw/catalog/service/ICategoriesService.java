package com.qw.catalog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qw.catalog.entity.ServiceCategories;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.catalog.service
 * @Project：home-serve
 * @name：ICategoriesService
 * @Date：2026/5/17 17:54
 * @Filename：ICategoriesService
 */
public interface ICategoriesService {

    List<ServiceCategories> list() throws JsonProcessingException;

    ServiceCategories getById(Long id) throws JsonProcessingException;
}
