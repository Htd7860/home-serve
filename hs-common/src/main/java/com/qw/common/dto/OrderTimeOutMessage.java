package com.qw.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author：qw
 * @Package：com.qw.common.dto
 * @Project：home-serve
 * @name：OrderTimeOutMessage
 * @Date：2026/5/28 14:34
 * @Filename：OrderTimeOutMessage
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderTimeOutMessage implements Serializable {
    Long id;
    Long templateId;
}
