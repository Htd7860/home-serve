package com.qw.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务分类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@TableName("service_categories")
public class ServiceCategories implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 分类名：家政保洁、搬家拉货等
     */
    private String name;

    /**
     * 分类图标
     */
    private String iconUrl;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 1启用 0禁用
     */
    private Integer status;

    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
        return "ServiceCategories{" +
        "id = " + id +
        ", name = " + name +
        ", iconUrl = " + iconUrl +
        ", sortOrder = " + sortOrder +
        ", status = " + status +
        ", createdAt = " + createdAt +
        "}";
    }
}
