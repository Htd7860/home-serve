package com.qw.catalog.mapper;

import com.qw.catalog.entity.PricingRules;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @Author：qw
 * @Package：com.qw.order.mapper
 * @Project：home-serve
 * @name：PricingRuleMapper
 * @Date：2026/5/18 11:17
 * @Filename：PricingRuleMapper
 */
@Mapper
public interface PricingRuleMapper {
    @Select("select * from pricing_rules where status ='ON'")
    List<PricingRules> getOnRules();
}
