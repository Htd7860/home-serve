package com.qw.catalog.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.qw.catalog.constant.CaffeineConstant;
import com.qw.catalog.constant.RedisConstant;
import com.qw.catalog.entity.PricingRules;
import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.mapper.PricingRuleMapper;
import com.qw.catalog.mapper.SkuMapper;
import com.qw.catalog.service.ISkuService;
import com.qw.common.exception.BizException;
import com.qw.common.utils.RandomTTL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    @Autowired
    Cache<String,Object> skuCache;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PricingRuleMapper pricingRuleMapper;
    @Override
    public List<ServiceSkus> getByCategory(Integer id) {
        List<ServiceSkus> skus = (List<ServiceSkus>) skuCache.get(CaffeineConstant.SKUS_PREFIX + id, key -> {
            String json = stringRedisTemplate.opsForValue().get(RedisConstant.SKUS_PREFIX + id);
            if ("".equals(json)) {
                return List.of();
            }
            List<ServiceSkus> list = null;
            try {
                list = objectMapper.readValue(json, new TypeReference<List<ServiceSkus>>() {
                });
            } catch (JsonProcessingException e) {
                log.error("{}", e);
                return null;
            }
            return list;
        });

        if (skus == null) {
            List<ServiceSkus> sk = skuMapper.getByCategory(id);
            if (sk == null) {
                stringRedisTemplate.opsForValue().set(RedisConstant.SKUS_PREFIX + id, "", RandomTTL.randomTTL(1L));
                return null;
            }
            skuCache.put(CaffeineConstant.SKUS_PREFIX + id, sk);
            String json = null;
            try {
                json = objectMapper.writeValueAsString(sk);
            } catch (JsonProcessingException e) {
                log.error("{}",e);
                throw new BizException("格式化失败");
            }
            stringRedisTemplate.opsForValue().set(RedisConstant.SKUS_PREFIX + id, json, RandomTTL.randomTTL(30));
            skus = sk;
        }
        if (skus.isEmpty()) {
            return null;
        }
        return skus;
    }

    @Override
    public ServiceSkus getById(Long id){
        ServiceSkus skus= (ServiceSkus) skuCache.get(CaffeineConstant.SKUS_SINGLE_PREFIX+id, key->{
            String json = stringRedisTemplate.opsForValue().get(RedisConstant.SKUS_SINGLE_PREFIX + id);
            if(json==null){return null;}
            if("".equals(json)){return new ServiceSkus();}
            ServiceSkus skus1=null;
            try {
                skus1 = objectMapper.readValue(json, ServiceSkus.class);
            } catch (JsonProcessingException e) {
                log.error("{}",e);
                return null;
            }
           return skus1;
        });

        if(skus==null){
            ServiceSkus byId = skuMapper.getById(id);
            if(byId==null){
                stringRedisTemplate.opsForValue().set(RedisConstant.SKUS_SINGLE_PREFIX+id,"",RandomTTL.randomTTL(1L));
                return null;
            }
            try {
                stringRedisTemplate.opsForValue().set(RedisConstant.SKUS_SINGLE_PREFIX+id,objectMapper.writeValueAsString(byId), RandomTTL.randomTTL(30L));
            } catch (JsonProcessingException e) {
                log.error("{}",e);
                throw new BizException("格式化失败");
            }
            skuCache.put(CaffeineConstant.SKUS_SINGLE_PREFIX+id,byId);
            skus=byId;
        }
        if(skus.getId()==null){return null;}
        return skus;
    }

    @Override
    public BigDecimal[] calculateMoney( LocalDateTime appointTime, BigDecimal baseMoney, boolean haveDis, Double distance) {
        List<PricingRules> list= (List<PricingRules>) skuCache.get(CaffeineConstant.PRICING_RULE_KEY, key->{
                String json = stringRedisTemplate.opsForValue().get(RedisConstant.PRICING_RULE_KEY);
                List<PricingRules> rules=null;
                if(json==null){return null;}
                try {
                    rules= objectMapper.readValue(json, new TypeReference<List<PricingRules>>() {
                   });
                } catch (JsonProcessingException e) {
                    log.error("{}",e);
                    return null;
                }
                return rules;
            });
        if(list==null){
            list=pricingRuleMapper.getOnRules();
            try {
                stringRedisTemplate.opsForValue().set(RedisConstant.PRICING_RULE_KEY,objectMapper.writeValueAsString(list),RandomTTL.randomTTL(30L));
            } catch (JsonProcessingException e) {
                log.error("{}",e);
            }
            skuCache.put(CaffeineConstant.PRICING_RULE_KEY,list);
        }
        if(list==null){return new BigDecimal[]{baseMoney,BigDecimal.ZERO,BigDecimal.ZERO};}
        BigDecimal[] res=new BigDecimal[3];
        BigDecimal sum=BigDecimal.ZERO;
        res[1]=BigDecimal.ZERO;
        for (PricingRules rules : list) {
            String ruleConfig = rules.getRuleConfig();
            JSONObject jsonObject = JSON.parseObject(ruleConfig);
            switch (rules.getRuleType()){
                case "TIME_SURCHARGE":
                    LocalTime now=appointTime.toLocalTime();
                    List<String> peakHours = jsonObject.getJSONArray("peakHours").toJavaList(String.class);
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
