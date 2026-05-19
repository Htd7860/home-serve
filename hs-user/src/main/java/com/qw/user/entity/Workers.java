package com.qw.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务者
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Workers implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务者ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * bcrypt密码
     */
    private String passwordHash;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 0未知 1男 2女
     */
    private Byte gender;

    /**
     * 所属服务品类
     */
    private Integer categoryId;

    /**
     * 0待审核 1通过 2拒绝 3禁用
     */
    private Integer status;

    /**
     * 0未认证 1已认证
     */
    private Integer verifyStatus;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;

    /**
     * 累计完成订单数
     */
    private Integer totalOrders;

    /**
     * 近30天接单率
     */
    private BigDecimal acceptRate;

    /**
     * 今日已接单数
     */
    private Integer todayOrders;

    /**
     * 0离线 1在线
     */
    private Integer onlineStatus;

    /**
     * 最后上报经度
     */
    private BigDecimal lastLng;

    /**
     * 最后上报纬度
     */
    private BigDecimal lastLat;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public BigDecimal getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(BigDecimal avgRating) {
        this.avgRating = avgRating;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getAcceptRate() {
        return acceptRate;
    }

    public void setAcceptRate(BigDecimal acceptRate) {
        this.acceptRate = acceptRate;
    }

    public Integer getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(Integer todayOrders) {
        this.todayOrders = todayOrders;
    }

    public Integer getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(Integer onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public BigDecimal getLastLng() {
        return lastLng;
    }

    public void setLastLng(BigDecimal lastLng) {
        this.lastLng = lastLng;
    }

    public BigDecimal getLastLat() {
        return lastLat;
    }

    public void setLastLat(BigDecimal lastLat) {
        this.lastLat = lastLat;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Workers{" +
        "id = " + id +
        ", phone = " + phone +
        ", passwordHash = " + passwordHash +
        ", name = " + name +
        ", idCard = " + idCard +
        ", avatarUrl = " + avatarUrl +
        ", gender = " + gender +
        ", categoryId = " + categoryId +
        ", status = " + status +
        ", verifyStatus = " + verifyStatus +
        ", avgRating = " + avgRating +
        ", totalOrders = " + totalOrders +
        ", acceptRate = " + acceptRate +
        ", todayOrders = " + todayOrders +
        ", onlineStatus = " + onlineStatus +
        ", lastLng = " + lastLng +
        ", lastLat = " + lastLat +
        ", createdAt = " + createdAt +
        ", updatedAt = " + updatedAt +
        "}";
    }
}
