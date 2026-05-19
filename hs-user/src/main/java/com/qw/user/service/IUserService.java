package com.qw.user.service;

import com.qw.user.dto.AddressRequest;
import com.qw.user.entity.UserAddresses;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.user.service
 * @Project：home-serve
 * @name：IUserService
 * @Date：2026/5/18 10:33
 * @Filename：IUserService
 */
public interface IUserService {
    List<UserAddresses> getAddressByUserId(Long id);

    void addAddress(AddressRequest addressRequest);

    UserAddresses getAddressById(Long id);


    void updateAddress(Long id, AddressRequest addressRequest);

    void deleteById(Long id);

    void changeDefaultAddress(Long id);
}
