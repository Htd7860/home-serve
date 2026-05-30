package com.qw.user.integration;

import com.qw.common.dto.AddressRequest;
import com.qw.common.entity.UserAddresses;
import com.qw.common.service.IUserService;
import com.qw.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @MockBean StringRedisTemplate stringRedisTemplate;
    @MockBean RedissonClient redissonClient;
    @MockBean(name = "skuBloomFilter")
    RBloomFilter<Object> skuBloomFilter;
    @MockBean(name = "categoryBloomFilter")
    RBloomFilter<Object> categoryBloomFilter;
    @MockBean RocketMQTemplate rocketMQTemplate;

    @Autowired IUserService userService;
    @Autowired JdbcTemplate jdbcTemplate;

    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM user_addresses");
        UserContext.set(1, TEST_USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== getAddressByUserId ====================

    @Test
    void getAddressByUserId_ReturnsList() {
        jdbcTemplate.update(
                "INSERT INTO user_addresses (user_id, contact_name, contact_phone, province, city, district, detail, lng, lat, is_default) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)",
                TEST_USER_ID, "张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号",
                new BigDecimal("120.123456"), new BigDecimal("30.123456"), 1);
        jdbcTemplate.update(
                "INSERT INTO user_addresses (user_id, contact_name, contact_phone, province, city, district, detail, lng, lat, is_default) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)",
                TEST_USER_ID, "李四", "13900139000", "浙江省", "杭州市", "拱墅区", "莫干山路200号",
                new BigDecimal("120.130000"), new BigDecimal("30.300000"), 0);

        List<UserAddresses> result = userService.getAddressByUserId(TEST_USER_ID);

        assertEquals(2, result.size());
        // 默认地址排前面
        assertEquals("张三", result.get(0).getContactName());
    }

    @Test
    void getAddressByUserId_NoData_ReturnsEmpty() {
        List<UserAddresses> result = userService.getAddressByUserId(TEST_USER_ID);
        assertTrue(result.isEmpty());
    }

    // ==================== addAddress ====================

    @Test
    void addAddress_Success() {
        AddressRequest req = buildAddressReq("张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号", (byte) 0);

        userService.addAddress(req);

        List<UserAddresses> list = userService.getAddressByUserId(TEST_USER_ID);
        assertEquals(1, list.size());
        assertEquals("张三", list.get(0).getContactName());
    }

    @Test
    void addAddress_AsDefault_RestoresPreviousDefault() {
        // 先加一个默认地址
        AddressRequest first = buildAddressReq("张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号", (byte) 1);
        userService.addAddress(first);
        // 再加一个默认地址
        AddressRequest second = buildAddressReq("李四", "13900139000", "浙江省", "杭州市", "拱墅区", "莫干山路200号", (byte) 1);
        userService.addAddress(second);

        List<UserAddresses> list = userService.getAddressByUserId(TEST_USER_ID);
        // 第二个应该是默认
        UserAddresses def = list.stream().filter(a -> a.getIsDefault() == 1).findFirst().orElse(null);
        assertNotNull(def);
        assertEquals("李四", def.getContactName());
        // 总共应该有 2 个地址，只有 1 个默认
        assertEquals(2, list.size());
        assertEquals(1, list.stream().filter(a -> a.getIsDefault() == 1).count());
    }

    // ==================== getAddressById ====================

    @Test
    void getAddressById_Exists_ReturnsAddress() {
        AddressRequest req = buildAddressReq("张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号", (byte) 1);
        userService.addAddress(req);

        List<UserAddresses> list = userService.getAddressByUserId(TEST_USER_ID);
        Long id = list.get(0).getId();

        UserAddresses result = userService.getAddressById(id);
        assertNotNull(result);
        assertEquals("张三", result.getContactName());
    }

    @Test
    void getAddressById_NotOwned_ReturnsNull() {
        // 另一个用户的数据
        jdbcTemplate.update(
                "INSERT INTO user_addresses (id, user_id, contact_name, contact_phone, province, city, district, detail) " +
                "VALUES (?,?,?,?,?,?,?,?)",
                100L, 999L, "别人", "13700000000", "北京", "北京", "朝阳", "xxx");

        UserAddresses result = userService.getAddressById(100L);
        assertNull(result);
    }

    // ==================== updateAddress ====================

    @Test
    void updateAddress_Success() {
        AddressRequest req = buildAddressReq("张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号", (byte) 1);
        userService.addAddress(req);
        List<UserAddresses> list = userService.getAddressByUserId(TEST_USER_ID);
        Long id = list.get(0).getId();

        AddressRequest update = buildAddressReq("张三改", "13800138001", "浙江省", "杭州市", "滨江区", "江南大道500号", (byte) 0);
        userService.updateAddress(id, update);

        UserAddresses result = userService.getAddressById(id);
        assertEquals("张三改", result.getContactName());
        assertEquals("滨江区", result.getDistrict());
    }

    @Test
    void updateAddress_SetAsDefault() {
        AddressRequest first = buildAddressReq("张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号", (byte) 0);
        userService.addAddress(first);
        List<UserAddresses> list = userService.getAddressByUserId(TEST_USER_ID);
        Long id = list.get(0).getId();

        AddressRequest update = buildAddressReq("张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号", (byte) 1);
        userService.updateAddress(id, update);

        UserAddresses result = userService.getAddressById(id);
        assertEquals(1, result.getIsDefault().intValue());
    }

    // ==================== deleteById ====================

    @Test
    void deleteById_Success() {
        AddressRequest req = buildAddressReq("张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号", (byte) 0);
        userService.addAddress(req);
        List<UserAddresses> list = userService.getAddressByUserId(TEST_USER_ID);
        Long id = list.get(0).getId();

        userService.deleteById(id);

        List<UserAddresses> after = userService.getAddressByUserId(TEST_USER_ID);
        assertTrue(after.isEmpty());
    }

    // ==================== changeDefaultAddress ====================

    @Test
    void changeDefaultAddress_Success() {
        AddressRequest first = buildAddressReq("张三", "13800138000", "浙江省", "杭州市", "西湖区", "文三路100号", (byte) 1);
        userService.addAddress(first);
        AddressRequest second = buildAddressReq("李四", "13900139000", "浙江省", "杭州市", "拱墅区", "莫干山路200号", (byte) 0);
        userService.addAddress(second);

        List<UserAddresses> list = userService.getAddressByUserId(TEST_USER_ID);
        Long secondId = list.get(1).getId(); // 非默认的那个

        userService.changeDefaultAddress(secondId);

        UserAddresses now = userService.getAddressById(secondId);
        assertEquals(1, now.getIsDefault().intValue());
    }

    private AddressRequest buildAddressReq(String name, String phone, String province,
                                            String city, String district, String detail, Byte isDefault) {
        AddressRequest req = new AddressRequest();
        req.setContactName(name);
        req.setContactPhone(phone);
        req.setProvince(province);
        req.setCity(city);
        req.setDistrict(district);
        req.setDetail(detail);
        req.setLng(new BigDecimal("120.123456"));
        req.setLat(new BigDecimal("30.123456"));
        req.setIsDefault(isDefault);
        return req;
    }
}
