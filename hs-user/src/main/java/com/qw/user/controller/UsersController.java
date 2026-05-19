package com.qw.user.controller;

import com.qw.user.dto.AddressRequest;
import com.qw.user.service.IUserService;
import com.qw.common.result.Result;
import com.qw.common.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * C端用户 前端控制器
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */

@Slf4j
@RestController
@RequestMapping("/users")
@Tag(name = "用户模块")
public class UsersController {
    @Autowired
    IUserService userServiceImpl;

    @Operation(summary = "按照用户id查询地址")
    @GetMapping("/me/addresses")
    public Result getMyAddress(){
        return Result.ok(userServiceImpl.getAddressByUserId(UserContext.getUserId()));
    }

    @Operation(summary = "新增个人地址")
    @PostMapping("/me/addresses")
    public Result addNewAddress(@Valid @RequestBody AddressRequest addressRequest){
        userServiceImpl.addAddress(addressRequest);
        return  Result.ok();
    }

    @Operation(summary = "按照id查询地址")
    @GetMapping("/me/addresses/{id}")
    public Result getAddress(@PathVariable Long id){
        return Result.ok(userServiceImpl.getAddressById(id));
    }

    @Operation(summary = "根据id修改地址")
    @PutMapping("/me/addresses/{id}")
    public Result updateAddress(@PathVariable Long id,@RequestBody AddressRequest addressRequest){
        userServiceImpl.updateAddress(id,addressRequest);
        return Result.ok();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/me/addresses/{id}")
    public Result deleteAddress(@PathVariable Long id){
        userServiceImpl.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "修改默认地址")
    @PutMapping("/me/addresses/{id}/default")
    public Result changeDefaultAddress(@PathVariable Long id){
        userServiceImpl.changeDefaultAddress(id);
        return Result.ok();
    }
}
