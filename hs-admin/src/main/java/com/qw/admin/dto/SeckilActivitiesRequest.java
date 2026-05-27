package com.qw.admin.dto;

import com.qw.admin.validator.OnCreate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author：qw
 * @Package：com.qw.marketing.dto
 * @Project：home-serve
 * @name：SeckilActivitiesRequest
 * @Date：2026/5/26 23:05
 * @Filename：SeckilActivitiesRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeckilActivitiesRequest {
    @NotEmpty
    String activityName;
    @NotNull
    Integer totalStock;
    @NotNull
    LocalDateTime startTime;
    @NotNull
    LocalDateTime endTime;
    @NotNull
    Long templateId;
    @NotNull
    Long categoryId;
}
