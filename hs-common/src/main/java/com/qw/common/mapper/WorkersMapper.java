package com.qw.common.mapper;

import com.qw.common.entity.Workers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 服务者 Mapper 接口
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Mapper
public interface WorkersMapper {

    @Select("select * from workers where phone=#{phone}")
    Workers selectByPhone(String phone);

    @Select("select * from workers where phone=#{phone}")
    Workers getByPhone(String phone);

    @Insert("insert into workers (phone,password_hash,name,id_card,gender) values (#{phone},#{passwordHash},#{name},#{idCard},#{gender})")
    void insertByOne(Workers worker);
}
