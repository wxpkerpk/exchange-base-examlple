package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * 用户表扩展
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Data
@Table(name = "ag_admin_v1.cola_user")
public class ColaUser implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //用户 ID
    @Id
	@Column(name = "invite_code")
    private String inviteCode;
	
	    //用户 ID关联
    @Column(name = "sys_user_id")
    private String sysUserId;

	    //用户 ID关联
    @Column(name = "nick_name")
    private String nickName;

	    //资金密码
    @Column(name = "money_password")
    private String moneyPassword;
	
	    //每日提现次数（不分币种）
    @Column(name = "withdraw_time")
    private Integer withdrawTime;
	
	    //账号是否被禁用
    @Column(name = "enable")
    private Integer enable;
	
	    //邀请人
    @Column(name = "inviter")
    private String inviter;
	
	    //个性签名
    @Column(name = "sign")
    private String sign;
	
	    //头像地址
    @Column(name = "avatar")
    private String avatar;
	

	/**
	 * 设置：用户 ID关联
	 */
	public void setSysUserId(String sysUserId) {
		this.sysUserId = sysUserId;
	}
	/**
	 * 获取：用户 ID关联
	 */
	public String getSysUserId() {
		return sysUserId;
	}
	/**
	 * 设置：资金密码
	 */
	public void setMoneyPassword(String moneyPassword) {
		this.moneyPassword = moneyPassword;
	}
	/**
	 * 获取：资金密码
	 */
	public String getMoneyPassword() {
		return moneyPassword;
	}
	/**
	 * 设置：每日提现次数（不分币种）
	 */
	public void setWithdrawTime(Integer withdrawTime) {
		this.withdrawTime = withdrawTime;
	}
	/**
	 * 获取：每日提现次数（不分币种）
	 */
	public Integer getWithdrawTime() {
		return withdrawTime;
	}

	public Integer getEnable() {
		return enable;
	}

	public void setEnable(Integer enable) {
		this.enable = enable;
	}

	/**
	 * 设置：邀请人
	 */
	public void setInviter(String inviter) {
		this.inviter = inviter;
	}
	/**
	 * 获取：邀请人
	 */
	public String getInviter() {
		return inviter;
	}
	/**
	 * 设置：个性签名
	 */
	public void setSign(String sign) {
		this.sign = sign;
	}
	/**
	 * 获取：个性签名
	 */
	public String getSign() {
		return sign;
	}
	/**
	 * 设置：头像地址
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	/**
	 * 获取：头像地址
	 */
	public String getAvatar() {
		return avatar;
	}
}
