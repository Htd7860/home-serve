package com.qw.catalog.constant;

/**
 * @Author：qw
 * @Package：com.qw.catalog.constant
 * @Project：home-serve
 * @name：RedisConstant
 * @Date：2026/5/17 21:27
 * @Filename：RedisConstant
 */
public class RedisConstant {
    public static final String ALL_CATEGORIES="cache:all_categories";
    public static final String CATEGORIES_PREFIX="cache:categories:";
    public static final String CATEGORIES_NOT_EXIST="该分类不存在";
    public static final String SKUS_PREFIX="cache:skus:";
    public static final String CATEGORIES_SKUS_NOT_EXIST="该分类的服务不存在";
    public static final String SKUS_SINGLE_PREFIX="cache:single_skus:";
    public static final String SKUS_SINGLE_NOT_FIND="该服务不存在";
    public static final String PRICING_RULE_KEY="cache:pricing_rule";
}
