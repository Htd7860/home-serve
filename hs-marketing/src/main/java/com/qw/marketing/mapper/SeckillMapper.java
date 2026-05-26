package com.qw.marketing.mapper;

import com.qw.marketing.entity.SeckillActivities;
import org.apache.ibatis.annotations.Insert;
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
    @Select("select * from seckill_activities where status=2 and now() between start_time and end_time")
    List<SeckillActivities> getAllSeckillActivities();

    @Select("select * from seckill_activities where id=#{id}")
    SeckillActivities getById(Long id);

    @Insert("insert into seckill_activities (activity_name, total_stock, limit_per_user, start_time, end_time, preheat_time, status, created_at, template_id, category_id) " +
            "values (#{activityName},#{totalStock},1,#{startTime},#{endTime},#{preheatTime},#{sattus},now(),#{templateId},#{categoryId})")
    int insert(SeckillActivities activities);
}
