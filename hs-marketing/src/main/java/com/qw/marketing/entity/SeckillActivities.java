package com.qw.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("seckill_activities")
public class SeckillActivities implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 关联优惠券模板
     */
    private Long templateId;

    /**
     * 券绑定品类
     */
    private Integer categoryId;

    /**
     * 总库存
     */
    private Integer totalStock;

    /**
     * 每人限购数量
     */
    private Integer limitPerUser;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 预热时间（开始前10分钟）
     */
    private LocalDateTime preheatTime;

    /**
     * 0草稿 1预热 2进行 3结束
     */
    private Integer status;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public Integer getLimitPerUser() {
        return limitPerUser;
    }

    public void setLimitPerUser(Integer limitPerUser) {
        this.limitPerUser = limitPerUser;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getPreheatTime() {
        return preheatTime;
    }

    public void setPreheatTime(LocalDateTime preheatTime) {
        this.preheatTime = preheatTime;
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
        return "SeckillActivities{" +
        "id = " + id +
        ", activityName = " + activityName +
        ", templateId = " + templateId +
        ", categoryId = " + categoryId +
        ", totalStock = " + totalStock +
        ", limitPerUser = " + limitPerUser +
        ", startTime = " + startTime +
        ", endTime = " + endTime +
        ", preheatTime = " + preheatTime +
        ", status = " + status +
        ", createdAt = " + createdAt +
        "}";
    }
}
