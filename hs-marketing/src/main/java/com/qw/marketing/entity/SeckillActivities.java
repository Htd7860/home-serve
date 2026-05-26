package com.qw.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
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


}
