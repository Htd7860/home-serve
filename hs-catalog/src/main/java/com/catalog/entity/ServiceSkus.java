package com.qw.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务SKU
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@TableName("service_skus")
public class ServiceSkus implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属分类
     */
    private Integer categoryId;

    /**
     * SKU名：日常保洁-2小时
     */
    private String name;

    /**
     * 服务内容描述
     */
    private String description;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 基础价（元）
     */
    private BigDecimal basePrice;

    /**
     * 服务时长（分钟），按数量计价为NULL
     */
    private Integer durationMinutes;

    /**
     * 计价单位
     */
    private String unit;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * ON / OFF
     */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ServiceSkus{" +
        "id = " + id +
        ", categoryId = " + categoryId +
        ", name = " + name +
        ", description = " + description +
        ", coverImage = " + coverImage +
        ", basePrice = " + basePrice +
        ", durationMinutes = " + durationMinutes +
        ", unit = " + unit +
        ", salesCount = " + salesCount +
        ", status = " + status +
        ", createdAt = " + createdAt +
        ", updatedAt = " + updatedAt +
        "}";
    }
}
