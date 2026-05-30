package com.qw.catalog.mapper;

import com.qw.catalog.entity.PricingRules;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("select * from pricing_rules where status = 1")
    List<PricingRules> getOnRules();

    @Select("select * from pricing_rules order by priority desc")
    List<PricingRules> listAll();

    @Insert("insert into pricing_rules (rule_type, rule_name, rule_config, priority, status, created_at) " +
            "values (#{ruleType},#{ruleName},#{ruleConfig},#{priority},#{status},now())")
    int insert(PricingRules rule);

    @Update("update pricing_rules set rule_type=#{ruleType},rule_name=#{ruleName},rule_config=#{ruleConfig}," +
            "priority=#{priority},status=#{status} where id=#{id}")
    int update(PricingRules rule);

    @Delete("delete from pricing_rules where id=#{id}")
    int delete(Integer id);
}
