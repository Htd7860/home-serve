package com.qw.common.service;

import com.qw.common.entity.UserAddresses;

/**
 * @Author：qw
 * @Package：com.qw.user.service
 * @Project：home-serve
 * @name：IAddressService
 * @Date：2026/5/18 10:43
 * @Filename：IAddressService
 */
public interface IAddressService {
    UserAddresses getById(Long id);
}
