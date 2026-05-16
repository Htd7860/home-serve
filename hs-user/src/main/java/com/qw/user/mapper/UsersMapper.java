package com.qw.user.mapper;

import com.qw.user.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.swagger.v3.oas.annotations.Operation;
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

    @Insert("insert into users (phone, password_hash, nickname, gender, created_at, updated_at) " +
            "values (#{phone},#{passwordHash},#{nickname},#{gender},#{createdAt},#{updatedAt})")
    void insertOne(Users user);

    @Select("select * from users where phone=#{phone}")
    Users getByPhone(String phone);

    @Update("update users set last_login_at =#{time} where id=#{userId}")
    void updateLastLoginTime(LocalDateTime time,Long userId);
}