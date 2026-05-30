package com.qw.catalog.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.PricingRules;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.mapper.PricingRuleMapper;
import com.qw.catalog.mapper.SkuMapper;
import com.qw.catalog.service.ISkuService;
import com.qw.common.cache.CacheTemplate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.catalog.service.impl
 * @Project：home-serve
 * @name：ISkuServiceImpl
 * @Date：2026/5/17 19:48
 * @Filename：ISkuServiceImpl
 */
@Slf4j
@Service
public class SkuServiceImpl implements ISkuService {
    @Autowired
    SkuMapper skuMapper;
    @Resource
    Cache<String,Object> skuCache;
    @Resource
    Cache<String,Object> pricingCache;
    @Autowired
    PricingRuleMapper pricingRuleMapper;
    @Resource
    RBloomFilter<Long> skuBloomFilter;
    @Autowired
    CacheTemplate cacheTemplate;

    @Override
    public List<ServiceSkus> getByCategory(Integer id) {
        return cacheTemplate.getList(RedisConstant.SKUS_PREFIX + id,id,ServiceSkus.class,a->skuMapper.getByCategory(a),skuCache);

    }

    @Override
    public ServiceSkus getById(Long id){
        if(!skuBloomFilter.contains(id)){return null;}
        return cacheTemplate.get(RedisConstant.SKUS_SINGLE_PREFIX + id,id,ServiceSkus.class,e->skuMapper.getById(id),skuCache);
    }

    @Override
    public BigDecimal[] calculateMoney( LocalDateTime appointTime, BigDecimal baseMoney, boolean haveDis, Double distance) {
        List<PricingRules> list = cacheTemplate.getList(RedisConstant.PRICING_RULE_KEY, null, PricingRules.class, k -> pricingRuleMapper.getOnRules(), pricingCache);
        if(list==null){return new BigDecimal[]{baseMoney,BigDecimal.ZERO,BigDecimal.ZERO};}
        BigDecimal[] res=new BigDecimal[3];
        BigDecimal sum=BigDecimal.ZERO;
        res[1]=BigDecimal.ZERO;
        res[2]=BigDecimal.ZERO;
        for (PricingRules rules : list) {
            String ruleConfig = rules.getRuleConfig();
            JSONObject jsonObject = JSON.parseObject(ruleConfig);
            if(jsonObject==null){continue;}
            switch (rules.getRuleType()){
                case "TIME_SURCHARGE":
                    LocalTime now=appointTime.toLocalTime();
                    List<String> peakHours = jsonObject.getJSONArray("peakHours").toJavaList(String.class);
                    if(peakHours==null){continue;}
                    for (String peakHour : peakHours) {
                        LocalTime from =LocalTime.parse(peakHour.split("-")[0]);
                        LocalTime to=LocalTime.parse(peakHour.split("-")[1]);
                        if(
                         now.isAfter(from)&&now.isBefore(to)){
                            BigDecimal temp=baseMoney.multiply(BigDecimal.valueOf(jsonObject.getDouble("surchargeRate")));
                            sum=sum.add(temp);
                            res[1]=temp;
                            break;
                        }
                    }
                    break;
                case "DISTANCE_SURCHARGE":
                    if(!haveDis){break;}
                    Integer freeKm = jsonObject.getInteger("freeKm");
                    Double pricePerKm = jsonObject.getDouble("pricePerKm");
                    Double maxFee=jsonObject.getDouble("maxFee");

                    int chargeKm=(int)(Math.ceil(distance-freeKm));
                    BigDecimal temp=BigDecimal.valueOf(Math.min(chargeKm*pricePerKm,maxFee));
                    sum=sum.add(temp);
                    res[2]=temp;
                    break;
            }
        }
        res[0]=sum.add(baseMoney);
        return res;
    }


}
