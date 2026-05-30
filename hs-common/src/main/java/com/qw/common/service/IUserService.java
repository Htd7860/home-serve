package com.qw.common.service;

import com.qw.common.dto.AddressRequest;
import com.qw.common.entity.UserAddresses;
import org.springframework.web.multipart.MultipartFile;

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

    String uploadAvatar(MultipartFile file);
}
