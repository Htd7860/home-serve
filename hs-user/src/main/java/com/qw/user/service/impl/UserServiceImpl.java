package com.qw.user.service.impl;

import com.qw.user.dto.AddressRequest;
import com.qw.user.entity.UserAddresses;
import com.qw.user.mapper.UserAddressesMapper;
import com.qw.user.service.IUserService;
import com.qw.common.utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.user.service.impl
 * @Project：home-serve
 * @name：UserServiceImpl
 * @Date：2026/5/18 10:33
 * @Filename：UserServiceImpl
 */
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    UserAddressesMapper userAddressesMapper;
    @Override
    public List<UserAddresses> getAddressByUserId(Long id) {
        return userAddressesMapper.getAddressByUserId(id);
    }

    @Override
    public void addAddress(AddressRequest addressRequest) {
        UserAddresses addresses= new UserAddresses();
        BeanUtils.copyProperties(addressRequest,addresses);
        Long userId=UserContext.getUserId();
        addresses.setUserId(userId);addresses.setCreatedAt(LocalDateTime.now());
       if( addressRequest.getIsDefault()==1){
           userAddressesMapper.restoreDefaultStatus(userId);
       }
        userAddressesMapper.insertAddress(addresses);
    }

    @Override
    public UserAddresses getAddressById(Long id) {
        return userAddressesMapper.getAddressById(id,UserContext.getUserId());
    }

    @Override
    public void updateAddress(Long id, AddressRequest addressRequest) {
        UserAddresses userAddresses=new UserAddresses();
        BeanUtils.copyProperties(addressRequest,userAddresses);
        userAddresses.setId(id);
        Long userId=UserContext.getUserId();
        userAddresses.setUserId(userId);
        if(addressRequest.getIsDefault()!=null&&addressRequest.getIsDefault()==1){userAddressesMapper.restoreDefaultStatus(userId);}
        userAddressesMapper.updateUserAddress(userAddresses);
    }

    @Override
    public void deleteById(Long id) {
        userAddressesMapper.deleteById(id,UserContext.getUserId());
    }

    @Override
    public void changeDefaultAddress(Long id) {
        userAddressesMapper.restoreDefaultStatus(UserContext.getUserId());
        userAddressesMapper.changeDefaultAddress(id,UserContext.getUserId());
    }
}
