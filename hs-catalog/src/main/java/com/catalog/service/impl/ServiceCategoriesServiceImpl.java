package com.qw.catalog.service.impl;

import com.qw.catalog.entity.ServiceCategories;
import com.qw.catalog.mapper.ServiceCategoriesMapper;
import com.qw.catalog.service.IServiceCategoriesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务分类 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
public class ServiceCategoriesServiceImpl extends ServiceImpl<ServiceCategoriesMapper, ServiceCategories> implements IServiceCategoriesService {

}
