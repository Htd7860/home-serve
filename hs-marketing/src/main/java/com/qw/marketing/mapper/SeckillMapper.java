package com.qw.marketing.mapper;

import com.qw.marketing.entity.SeckillActivities;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.marketing.mapper.xml
 * @Project：home-serve
 * @name：SeckillMapper
 * @Date：2026/5/26 14:47
 * @Filename：SeckillMapper
 */
@Mapper
public interface SeckillMapper {
    @Select("select * from seckill_activities")
    List<SeckillActivities> getAllSeckillActivities();
}
