package com.qw.common.mapper;

import com.qw.common.entity.Users;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * <p>
 * C端用户 Mapper 接口
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Mapper
public interface UsersMapper{

    @Select("select * from users where id=#{id}")
    Users selectById(Long id);

    @Select("select * from users where phone=#{phone}")
    Users selectByPhone(String phone);

    @Insert("insert into users (phone, password_hash, nickname, gender, created_at, updated_at,role) " +
            "values (#{phone},#{passwordHash},#{nickname},#{gender},#{createdAt},#{updatedAt},#{loginType})")
    void insertOne(Users user,Integer loginType);


    @Update("update users set last_login_at =#{time} where id=#{userId}")
    void updateLastLoginTime(LocalDateTime time,Long userId);

    @Update("update users set avatar_url = #{avatarUrl} where id = #{id}")
    int updateAvatar(Long id, String avatarUrl);
}