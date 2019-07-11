package com.bitcola.me.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Column;

/**
 * 用户
 *
 * @author zkq
 * @create 2018-07-16 19:38
 **/
@Data
public class ColaUserEntity {

    /**
     * 系统 ID
     */
    @JSONField(serialize = false)
    String id;
    /**
     * 邀请码
     */
    @JSONField(serialize = false)
    String inviteCode;
    /**
     * 系统关联 ID
     */
    String sysUserID;

    /**
     * 用户昵称
     */
    String username;
    /**
     * 手机号
     */
    String telPhone;
    String areaCode;
    Long time;
    /**
     * 邮箱
     */
    String email;
    /**
     * 登陆密码
     */
    @JSONField(serialize = false)
    String password;
    /**
     * 资金密码
     */
    @JSONField(serialize = false)
    String moneyPassword;
    /**
     * 邀请人
     */
    String inviter;
    /**
     * 是否禁用 1为正常,0位禁用
     */
    @JSONField(serialize = false)
    int enable;
    /**
     * 每日提现次数限制
     */
    @JSONField(serialize = false)
    int withdrawTime;

    /**
     * 个性签名
     */
    String sign;

    /**
     * 头像
     */
    String avatar;

    /**
     * 昵称
     */
    String nickName;

    Integer isUsernameUpdate;

    @JSONField(serialize = false)
    String googleSecretKey;

    String language;
    String ip;
    String antiPhishingCode;

    public String getSysUserID() {
        return sysUserID;
    }

    public void setSysUserID(String sysUserID) {
        this.sysUserID = sysUserID;
    }

    public String getTelPhone() {
        return telPhone;
    }

    public void setTelPhone(String telPhone) {
        this.telPhone = telPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    @JsonIgnore
    public String getMoneyPassword() {
        return moneyPassword;
    }

    public void setMoneyPassword(String moneyPassword) {
        this.moneyPassword = moneyPassword;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    @JsonIgnore
    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    @JsonIgnore
    public int getWithdrawTime() {
        return withdrawTime;
    }

    public void setWithdrawTime(int withdrawTime) {
        this.withdrawTime = withdrawTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
