package com.qw.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 动态定价规则
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@TableName("pricing_rules")
public class PricingRules implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * DISTANCE_SURCHARGE / TIME_SURCHARGE
     */
    private String ruleType;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则参数JSON
     */
    private String ruleConfig;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * ON / OFF
     */
    private Integer status;

    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(String ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PricingRules{" +
        "id = " + id +
        ", ruleType = " + ruleType +
        ", ruleName = " + ruleName +
        ", ruleConfig = " + ruleConfig +
        ", priority = " + priority +
        ", status = " + status +
        ", createdAt = " + createdAt +
        "}";
    }
}
