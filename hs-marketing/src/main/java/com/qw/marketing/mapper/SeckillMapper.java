package com.qw.marketing.mapper;

import com.qw.marketing.entity.SeckillActivities;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Insert("insert into seckill_activities (activity_name, total_stock, limit_per_user, start_time, end_time, preheat_time, status, created_at, template_id, category_id) values " +
            "(#{activityName},#{totalStock},1,#{startTime},#{endTime},#{preheatTime},0,now(),#{templateId},#{categoryId})")
    int insert(SeckillActivities seckillActivities);

    @Update("update seckill_activities set activity_name=#{activityName},total_stock=#{totalStock},limit_per_user=#{limitPerUser}," +
            "start_time=#{startTime},end_time=#{endTime},preheat_time=#{preheatTime},status=#{status}," +
            "template_id=#{templateId},category_id=#{categoryId} where id=#{id}")
    int update(SeckillActivities activities);

    @Update("update seckill_activities set status=#{status} where id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("select * from seckill_activities order by created_at desc")
    List<SeckillActivities> listAll();

    @Delete("delete from seckill_activities where id=#{id}")
    int delete(Long id);

    @Select("select * from seckill_activities where status=0 and now() >= preheat_time")
    List<SeckillActivities> selectPendingPreheat();

    @Update("update seckill_activities set status=2 where status=1 and now() >= start_time")
    int batchStart();

    @Update("update seckill_activities set status=3 where status=2 and now() > end_time")
    int batchEnd();
}
