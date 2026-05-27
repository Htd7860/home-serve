package com.qw.admin.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.qw.admin.dto.CouponTemplatesRequest;
import com.qw.admin.dto.PricingRuleRequest;
import com.qw.admin.dto.SeckilActivitiesRequest;
import com.qw.admin.dto.ServiceCategoryRequest;
import com.qw.admin.dto.SkuServiceRequest;
import com.qw.admin.service.IAdminService;
import com.qw.catalog.constant.CaffeineConstant;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.ServiceCategories;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.entity.PricingRules;
import com.qw.catalog.mapper.CategoriesMapper;
import com.qw.catalog.mapper.PricingRuleMapper;
import com.qw.catalog.mapper.SkuMapper;
import com.qw.common.entity.Workers;
import com.qw.common.exception.BizException;
import com.qw.common.mapper.WorkersMapper;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.SeckillActivities;
import com.qw.marketing.mapper.CouponsMapper;
import com.qw.marketing.mapper.SeckillMapper;
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
    @Autowired
    SeckillMapper seckillMapper;
    @Autowired
    CouponsMapper couponsMapper;
    @Autowired
    PricingRuleMapper pricingRuleMapper;
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

    @Override
    public void addActivities(SeckilActivitiesRequest req) {
        if(req.getStartTime().plusMinutes(-10).isBefore(LocalDateTime.now())||req.getStartTime().isAfter(req.getEndTime())){
            throw new BizException("活动时间设置错误");
        }
        SeckillActivities activities=new SeckillActivities();
        BeanUtils.copyProperties(req,activities);
        activities.setPreheatTime(req.getStartTime().minusMinutes(10));
        int rows=seckillMapper.insert(activities);
        if(rows==0){
            throw new BizException("活动创建失败");
        }
    }

    @Override
    public void updateActivities(Long id, SeckilActivitiesRequest req) {
        SeckillActivities activities=seckillMapper.getById(id);
        if(activities==null){throw new BizException("活动不存在");}
        int status=activities.getStatus();
        String stockKey= com.qw.marketing.constant.RedisConstant.SECKILL_STOCK_PREFIX+id;
        if(status==0){
            if(req.getStartTime().minusMinutes(10).isBefore(LocalDateTime.now())){
                throw new BizException("时间修改失败");
            }
            BeanUtils.copyProperties(req,activities);
            activities.setPreheatTime(req.getStartTime().minusMinutes(10));
        }else if(status==1){
            activities.setTotalStock(req.getTotalStock());
            activities.setEndTime(req.getEndTime());
            String old=stringRedisTemplate.opsForValue().get(stockKey);
            if (old!=null) {
                Long stock=Long.valueOf(old);
                Long dis=Long.valueOf(req.getTotalStock())-stock;
                stringRedisTemplate.opsForValue().increment(stockKey,dis);
            }
        }else if(status==2){
            activities.setEndTime(req.getEndTime());
        }else if(status==3){
            throw new BizException("不可修改");
        }else{
            throw new BizException("状态错误");
        }
        seckillMapper.update(activities);
        stringRedisTemplate.delete(com.qw.marketing.constant.RedisConstant
                .SECKILL_ACTIVITIES);
    }

    @Override
    public List<SeckillActivities> getAllActivities() {
        return seckillMapper.listAll();
    }

    @Override
    public List<CouponTemplates> getAllTemplates() {
        return couponsMapper.listAllTemplates();
    }

    @Override
    public void addTemplate(CouponTemplatesRequest req) {
        CouponTemplates template = new CouponTemplates();
        BeanUtils.copyProperties(req, template);
        int rows = couponsMapper.insertTemplate(template);
        if (rows == 0) {
            throw new BizException("创建失败");
        }
    }

    @Override
    public void updateTemplate(Long id, CouponTemplatesRequest req) {
        CouponTemplates template = couponsMapper.getTemplatesById(id);
        if (template == null) {
            throw new BizException("模板不存在");
        }
        BeanUtils.copyProperties(req, template);
        template.setId(id);
        int rows = couponsMapper.updateTemplate(template);
        if (rows == 0) {
            throw new BizException("修改失败");
        }
    }

    @Override
    public List<PricingRules> getAllPricingRules() {
        return pricingRuleMapper.listAll();
    }

    @Override
    public void addPricingRule(PricingRuleRequest req) {
        PricingRules rule = new PricingRules();
        rule.setRuleType(req.getRuleType());
        rule.setRuleName(req.getRuleName());
        rule.setRuleConfig(req.getRuleConfig());
        rule.setPriority(req.getPriority());
        rule.setStatus(req.getStatus());
        int rows = pricingRuleMapper.insert(rule);
        if (rows == 0) {
            throw new BizException("创建失败");
        }
        cache.invalidate(com.qw.catalog.constant.CaffeineConstant.PRICING_RULE_KEY);
        stringRedisTemplate.delete(com.qw.catalog.constant.RedisConstant.PRICING_RULE_KEY);
    }

    @Override
    public void updatePricingRule(Integer id, PricingRuleRequest req) {
        PricingRules rule = new PricingRules();
        rule.setId(id);
        rule.setRuleType(req.getRuleType());
        rule.setRuleName(req.getRuleName());
        rule.setRuleConfig(req.getRuleConfig());
        rule.setPriority(req.getPriority());
        rule.setStatus(req.getStatus());
        int rows = pricingRuleMapper.update(rule);
        if (rows == 0) {
            throw new BizException("修改失败");
        }
        cache.invalidate(com.qw.catalog.constant.CaffeineConstant.PRICING_RULE_KEY);
        stringRedisTemplate.delete(com.qw.catalog.constant.RedisConstant.PRICING_RULE_KEY);
    }

    @Override
    public void deletePricingRule(Integer id) {
        int rows = pricingRuleMapper.delete(id);
        if (rows == 0) {
            throw new BizException("删除失败");
        }
        cache.invalidate(com.qw.catalog.constant.CaffeineConstant.PRICING_RULE_KEY);
        stringRedisTemplate.delete(com.qw.catalog.constant.RedisConstant.PRICING_RULE_KEY);
    }

    @Override
    public void deleteActivity(Long id) {
        SeckillActivities activities=seckillMapper.getById(id);
        if(activities==null){throw new BizException("活动不存在");}
        if(activities.getStatus()!=0){throw new BizException("活动无法删除");}
        int rows = seckillMapper.delete(id);
        if(rows==0){
            throw new BizException("删除失败");
        }

        stringRedisTemplate.delete(com.qw.marketing.constant.RedisConstant
                .SECKILL_ACTIVITIES);
    }
}
