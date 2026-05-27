package com.qw.common.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qw.common.entity.Notifications;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.common.mapper
 * @Project：home-serve
 * @name：NotificationMapper
 * @Date：2026/5/27 13:59
 * @Filename：NotificationMapper
 */
@Mapper
public interface NotificationMapper {
    @Select("select * from notifications where receiver_type=#{type} and receiver_id=#{userId} order by created_at desc")
    List<Notifications> listByPage(Page page, @Param("userId") Long userId, @Param("type") Integer type);

    @Insert("insert into notifications (receiver_type, receiver_id, title, content, notification_type, related_order_id) values " +
            "(#{receiverType},#{receiverId},#{title},#{content},#{notificationType},#{relatedOrderId})")
    int insert(Notifications notifications);

    @Update("update notifications set is_read=1 where id=#{id} and receiver_type=#{type} and receiver_id=#{userId}")
    int readById(@Param("id") Long id, @Param("userId") Long userId, @Param("type") Integer type);

    @Update("update notifications set is_read=1 where receiver_type=#{type} and receiver_id=#{userId}")
    int readAll(@Param("userId") Long userId, @Param("type") Integer type);

    @Select("select count(*) from notifications where receiver_type=#{type} and receiver_id=#{userId} and is_read=0")
    int countUnread(@Param("userId") Long userId, @Param("type") Integer type);
}
