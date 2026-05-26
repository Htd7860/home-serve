package com.qw.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Builder
@TableName("service_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
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


}
