package com.qw.common.mapper;

import com.qw.common.entity.Workers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

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

    @Select("select * from workers where id=#{id}")
    Workers selectById(Long id);

    @Update("update workers set online_status =#{status} where id=#{id}")
    void updateOnlineStatus(Integer status,Long id);

    @Update("update workers set last_lng=#{lng},last_lat=#{lat},updated_at=NOW() where id =#{id}")
    void updateWorkerLocation(Long id, BigDecimal lng,BigDecimal lat);
}
