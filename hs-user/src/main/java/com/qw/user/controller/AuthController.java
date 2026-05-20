package com.qw.user.controller;

import com.qw.common.dto.*;
import com.qw.user.constant.LoginConstant;
import com.qw.user.constant.RedisConstant;
import com.qw.user.constant.RegisterConstant;
import com.qw.common.entity.Users;
import com.qw.common.entity.Workers;
import com.qw.common.mapper.UsersMapper;
import com.qw.common.mapper.WorkersMapper;
import com.qw.common.result.Result;
import com.qw.common.utils.JwtUtils;
import com.qw.common.utils.PasswordUtils;
import com.qw.common.utils.RandomAuthCode;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @Author：qw
 * @Package：com.qw.user.controller
 * @Project：home-serve
 * @name：AuthController
 * @Date：2026/5/16 11:38
 * @Filename：AuthController
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name="用户认证")
public class AuthController {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    UsersMapper usersMapper;
   @Autowired
    WorkersMapper workersMapper;

    @Operation(summary = "发送验证码(登录)")
    @GetMapping("/send-code")
    public Result sendCode(String phone) {
        boolean flag = phone.matches("^1[3-9]\\d{9}$");
        if (!flag) {
            return Result.fail(LoginConstant.PHONE_FORMAT_ERROR);
        }
        Boolean success = stringRedisTemplate.hasKey(RedisConstant.LOGIN_PHONE_KEY_PREFIX + phone);
        if (success) {
            return Result.fail(LoginConstant.PHONE_RETRY_ERROR);
        }
        String code = RandomAuthCode.get(6);
        log.info("登录验证码:{}", code);
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PHONE_KEY_PREFIX + phone, code, 60, TimeUnit.SECONDS);
        //todo 完成验证码发送功能
        return Result.ok();
    }

    @Operation(summary = "发送验证码(注册)")
    @GetMapping("/send-code-to-register")
    public Result sendCodeToRegister(String phone) {
        boolean flag = phone.matches("^1[3-9]\\d{9}$");
        if (!flag) {
            return Result.fail(LoginConstant.PHONE_FORMAT_ERROR);
        }
        Boolean success = stringRedisTemplate.hasKey(RedisConstant.REGISTER_PHONE_KEY_PREFIX + phone);
        if (success) {
            return Result.fail(LoginConstant.PHONE_RETRY_ERROR);
        }
        String code = RandomAuthCode.get(6);
        log.info("注册验证码:{}", code);
        stringRedisTemplate.opsForValue().set(RedisConstant.REGISTER_PHONE_KEY_PREFIX + phone, code, 60, TimeUnit.SECONDS);
        //todo 完成验证码发送功能
        return Result.ok();
    }


    @Operation(summary = "登陆验证(密码)")
    @PostMapping("/loginByPwd")
    public Result loginByPwd(@Validated(OnPwdLogin.class) @RequestBody LoginRequest loginRequest) {

        String phone = loginRequest.getPhone();

        if (loginRequest.getLoginType() == LoginConstant.LOGIN_TYPE_WORKER) {
            Workers workers = workersMapper.selectByPhone(phone);
            if (workers == null) {
                return Result.fail(LoginConstant.PHONE_NOT_EXISTS);
            }
            if (PasswordUtils.match(loginRequest.getPassword(), workers.getPasswordHash())) {
                LoginResponse response=workersLoginCallback(loginRequest,phone);
                return Result.ok(response);
            }
        } else  {
            Users user = usersMapper.selectByPhone(phone);
            if (user == null) {
                return Result.fail(LoginConstant.PHONE_NOT_EXISTS);
            }
            if (PasswordUtils.match(loginRequest.getPassword(), user.getPasswordHash()) ) {
                LoginResponse response = usersLoginCallback(phone, loginRequest);
                return Result.ok(response);
            }
        }
        return Result.fail(LoginConstant.LOGIN_PASSWORD_ERROR);
    }

    @Operation(summary = "登陆验证(验证码)")
    @PostMapping("/loginByCode")
    public Result loginByCode(@Validated(OnCodeLogin.class) @RequestBody LoginRequest loginRequest) {

        String phone = loginRequest.getPhone();

        if (loginRequest.getLoginType() == LoginConstant.LOGIN_TYPE_USER) {
            Users user = usersMapper.selectByPhone(phone);
            if (user == null) {
                return Result.fail(LoginConstant.PHONE_NOT_EXISTS);
            }
            String code=stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_PHONE_KEY_PREFIX+phone);
            if (code!=null&&code.equals(loginRequest.getCode())) {
                LoginResponse response = usersLoginCallback(phone, loginRequest);
                return Result.ok(response);
            }
            return Result.fail(LoginConstant.LOGIN_CODE_ERROR);
        } else  {
            Workers workers = workersMapper.selectByPhone(phone);
            if (workers == null) {
                return Result.fail(LoginConstant.PHONE_NOT_EXISTS);
            }
            String storedCode = stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_PHONE_KEY_PREFIX + phone);
            if (storedCode != null && loginRequest.getCode().equals(storedCode)) {
                LoginResponse response=workersLoginCallback(loginRequest,phone);
                return Result.ok(response);
            }
            return Result.fail(LoginConstant.LOGIN_CODE_ERROR);
        }
    }

    @Operation(summary = "用户密码注册")
    @PostMapping("/registerByPwd")
    public Result registerByPwd(@Validated(OnPasswordRegister.class) @RequestBody RegisterRequest registerRequest) {

        String phone = registerRequest.getPhone();
        Users byPhone = usersMapper.selectByPhone(phone);
        if (byPhone != null) {
            return Result.fail(RegisterConstant.REGISTER_ALREADY_HAVE);
        }
        if (!registerRequest.getPassword().equals(registerRequest.getRePassword())) {
            return Result.fail(RegisterConstant.REGISTER_PASSWORD_NOT_SAME);
        }
        insertUsers(registerRequest);
        return Result.ok();
    }

    @Operation(summary = "工作者密码注册")
    @PostMapping("/registerByPwdToWorker")
    public Result registerByPwdToWorkers(@Validated({OnPasswordRegister.class,RegisterForWorker.class}) @RequestBody RegisterRequest registerRequest) {

        String phone = registerRequest.getPhone();
        Workers byPhone = workersMapper.getByPhone(phone);
        if (byPhone != null) {
            return Result.fail(RegisterConstant.REGISTER_ALREADY_HAVE);
        }
        if (!registerRequest.getPassword().equals(registerRequest.getRePassword())) {
            return Result.fail(RegisterConstant.REGISTER_PASSWORD_NOT_SAME);
        }

        Workers workers=Workers.builder().idCard(registerRequest.getIdCard()).passwordHash(PasswordUtils.encode(registerRequest.getPassword())
        ).gender(registerRequest.getGender()).name(registerRequest.getNickName()).phone(registerRequest.getPhone()).build();
        workersMapper.insertByOne(workers);
        return Result.ok();
    }

    @Operation(summary = "用户验证码注册")
    @PostMapping("/registerByCode")
    public Result registerByCode(@Validated(OnCodeRegister.class) @RequestBody RegisterRequest registerRequest) {

        String code = registerRequest.getCode();

        if (!code.equals(stringRedisTemplate.opsForValue().get(RedisConstant.REGISTER_PHONE_KEY_PREFIX + registerRequest.getPhone()))) {
            return Result.fail(RegisterConstant.REGISTER_CODE_ERROR);
        }
        insertUsersWithoutPassword(registerRequest);
        return Result.ok();
    }

    @Operation(summary = "刷新token")
    @PostMapping("/refreshToken")
    public Result<LoginResponse> refreshToken(@RequestBody RefreshRequest refreshRequest){
        String olderFreshToken = refreshRequest.getRefreshToken();
        Claims claims = JwtUtils.parseToken(olderFreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        String loginType=(String) claims.get("loginType");
        String s = stringRedisTemplate.opsForValue().get(RedisConstant.AUTH_REFRESH_TOKEN + userId);
        if(s==null||!s.equals(olderFreshToken)){
            return Result.fail(LoginConstant.REFRESH_ERROR);
        };
        String newToken=JwtUtils.createToken(userId,loginType,LoginConstant.FIXED_JWT_TIME,null);
        String newFreshToken=JwtUtils.createToken(userId,loginType,LoginConstant.REFRESH_JWT_TIME,null);
        stringRedisTemplate.opsForValue().set(RedisConstant.AUTH_REFRESH_TOKEN+userId,newFreshToken,7,TimeUnit.DAYS);
        return Result.ok(LoginResponse.builder().userId(userId).token(newToken).refreshToken(newFreshToken)
                .loginType(Integer.parseInt(loginType)).build());
    }

    private void insertUsers(RegisterRequest registerRequest) {
        Users users = Users.builder().nickname(registerRequest.getNickName())
                .phone(registerRequest.getPhone())
                .passwordHash(PasswordUtils.encode(registerRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        usersMapper.insertOne(users,registerRequest.getRegisterType());
    }

    private void insertUsersWithoutPassword(RegisterRequest registerRequest) {
        Users users = Users.builder().nickname(registerRequest.getNickName())
                .phone(registerRequest.getPhone())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        usersMapper.insertOne(users,registerRequest.getRegisterType());
    }

    private LoginResponse usersLoginCallback(String phone, LoginRequest loginRequest) {
        Users users = usersMapper.selectByPhone(phone);
        Long userId = users.getId();
        usersMapper.updateLastLoginTime(LocalDateTime.now(), userId);
        String token = JwtUtils.createToken(userId, loginRequest.getLoginType().toString(), LoginConstant.FIXED_JWT_TIME, null);
        String refreshToken = JwtUtils.createToken(userId, loginRequest.getLoginType().toString(), LoginConstant.REFRESH_JWT_TIME, null);
        stringRedisTemplate.opsForValue().set(RedisConstant.AUTH_REFRESH_TOKEN + userId, refreshToken, 7, TimeUnit.DAYS);
        return new LoginResponse(token, refreshToken, userId, loginRequest.getLoginType(), users.getAvatarUrl(), users.getNickname());
    }

    private  LoginResponse workersLoginCallback(LoginRequest loginRequest,String phone){
        Workers worker = workersMapper.getByPhone(phone);
        Long userId = worker.getId();
        String token = JwtUtils.createToken(userId, loginRequest.getLoginType().toString(), LoginConstant.FIXED_JWT_TIME, null);
        String refreshToken = JwtUtils.createToken(userId, loginRequest.getLoginType().toString(), LoginConstant.REFRESH_JWT_TIME, null);
        stringRedisTemplate.opsForValue().set(RedisConstant.AUTH_REFRESH_TOKEN + userId, refreshToken, 7, TimeUnit.DAYS);
        return new LoginResponse(token, refreshToken, userId, loginRequest.getLoginType(), worker.getAvatarUrl(), worker.getName());
    }
}
