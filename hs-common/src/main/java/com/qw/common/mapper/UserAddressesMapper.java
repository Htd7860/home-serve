package com.qw.common.mapper;

import com.qw.common.entity.UserAddresses;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 * 用户地址簿 Mapper 接口
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Mapper
public interface UserAddressesMapper {
    @Select("select * from user_addresses where user_id=#{userId} order by is_default desc")
    List<UserAddresses> getAddressByUserId(Long userId);

    @Insert("insert into user_addresses (user_id, contact_name, contact_phone, province, city, district, detail, lng, lat,created_at,is_default) " +
            "values (#{userId},#{contactName},#{contactPhone},#{province},#{city},#{district},#{detail},#{lng},#{lat},#{createdAt},#{isDefault})")
    void insertAddress(UserAddresses userAddresses);

    @Select("select * from user_addresses where id=#{id} and user_id=#{userId}")
    UserAddresses getAddressById(Long id,Long userId);

    void updateUserAddress(UserAddresses userAddresses);

    @Delete("delete from user_addresses where id=#{id} and user_id=#{userId}")
    void deleteById(Long id, Long userId);


    @Update("update user_addresses set is_default=1 where id=#{id} and user_id=#{userId}")
    void changeDefaultAddress(Long id, Long userId);

    @Update("update user_addresses set is_default=0 where user_id=#{userId}")
    void restoreDefaultStatus(Long userId);
}
