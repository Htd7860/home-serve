package com.qw.admin.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.admin.dto.ServiceCategoryRequest;
import com.qw.admin.dto.SkuServiceRequest;
import com.qw.admin.service.IAdminService;
import com.qw.catalog.constant.CaffeineConstant;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.ServiceCategories;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.mapper.CategoriesMapper;
import com.qw.catalog.mapper.SkuMapper;
import com.qw.common.constant.RocketMQConstant;
import com.qw.common.entity.Workers;
import com.qw.common.exception.BizException;
import com.qw.common.mapper.WorkersMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @Author：qw
 * @Package：com.qw.admin.service.impl
 * @Project：home-serve
 * @name：AdminServiceImpl
 * @Date：2026/5/26 18:42
 * @Filename：AdminServiceImpl
 */
@Service
public class AdminServiceImpl implements IAdminService {
    @Autowired
    CategoriesMapper categoriesMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    Cache<String,Object> cache;
    @Autowired
    SkuMapper skuMapper;
    @Autowired
    WorkersMapper workersMapper;
    @Override
    public void insertWithOne(ServiceCategoryRequest req) {
        ServiceCategories categories=new ServiceCategories();
        BeanUtils.copyProperties(req,categories);
        categoriesMapper.insertWithOne(categories);
        stringRedisTemplate.delete(RedisConstant.ALL_CATEGORIES);
        cache.invalidate(CaffeineConstant.ALL_CATEGORIES);
    }

    @Override
    public void updateCategoryByid(Integer id,ServiceCategoryRequest req) {
        ServiceCategories categories=new ServiceCategories();
        BeanUtils.copyProperties(req,categories);
        categories.setId(id);
        int rows = categoriesMapper.updateCategoryById(categories);
        if(rows==0){throw new BizException("该分类不存在");}
        stringRedisTemplate.delete(RedisConstant.CATEGORIES_PREFIX+id);
        stringRedisTemplate.delete(RedisConstant.ALL_CATEGORIES);
        cache.invalidate(CaffeineConstant.ALL_CATEGORIES);
        cache.invalidate(CaffeineConstant.CATEGORY_PREFIX+id);
    }

    @Override
    public void deleteCategoryById(Integer id) {
        List<ServiceSkus> list = skuMapper.getByCategory(id);
        if(!(list==null||list.size()==0)){
            throw new BizException("该分类下还有sku服务");
        }
        int rows = categoriesMapper.deleteById(id);
        if(rows==0){
            throw new BizException("该分类不存在");
        }
        stringRedisTemplate.delete(RedisConstant.CATEGORIES_PREFIX+id);
        stringRedisTemplate.delete(RedisConstant.ALL_CATEGORIES);
        cache.invalidate(CaffeineConstant.ALL_CATEGORIES);
        cache.invalidate(CaffeineConstant.CATEGORY_PREFIX+id);
    }

    @Override
    public void addSku(SkuServiceRequest req) {
        ServiceSkus sku=new ServiceSkus();
        BeanUtils.copyProperties(req,sku);
        int rows=0;
        if(req.getCoverImage()==null){
            rows=skuMapper.insertWithNoImg(sku);
        }else{
            rows=skuMapper.insertWithImg(sku);
        }
        if(rows==0){throw new BizException("插入失败");}
        stringRedisTemplate.delete(RedisConstant.SKUS_PREFIX+req.getCategoryId());
        cache.invalidate(CaffeineConstant.SKUS_PREFIX+req.getCategoryId());
    }

    @Override
    public void updateSku(Long id, SkuServiceRequest req) {
        ServiceSkus sku=new ServiceSkus();
        BeanUtils.copyProperties(req,sku);
        sku.setId(id);sku.setUpdatedAt(LocalDateTime.now());
        int rows=skuMapper.updateSku(sku);
        if(rows==0){
            throw new BizException("修改失败");
        }
        stringRedisTemplate.delete(RedisConstant.SKUS_PREFIX+req.getCategoryId());
        cache.invalidate(CaffeineConstant.SKUS_PREFIX+req.getCategoryId());
        stringRedisTemplate.delete(RedisConstant.SKUS_SINGLE_PREFIX+id);
        cache.invalidate(CaffeineConstant.SKUS_SINGLE_PREFIX+id);
    }

    @Override
    public List<Workers> getPendingList() {
         return workersMapper.getPendingWorkers();
    }

    @Override
    public void approveById(Long id) {
      int rows=workersMapper.approveById(id);
      if(rows==0){throw new BizException("状态修改失败");}
    }

    @Override
    public void rejectById(Long id) {
        int rows= workersMapper.rejectById(id);
        if(rows==0){throw new BizException("状态修改失败");}
    }
}
