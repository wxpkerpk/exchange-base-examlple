package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * ico用户信息表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-29 14:43:56
 */
@Data
@Table(name = "ag_admin_v1.cola_ico_user")
public class ColaIcoUser implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //
    @Column(name = "user_id")
    private String userId;
	
	    //
    @Column(name = "first_name")
    private String firstName;
	
	    //
    @Column(name = "last_name")
    private String lastName;
	
	    //性别
    @Column(name = "gender")
    private String gender;
	
	    //生日
    @Column(name = "birthday")
    private String birthday;
	
	    //邮箱
    @Column(name = "email")
    private String email;
	
	    //国际区号
    @Column(name = "area_code")
    private String areaCode;
	
	    //电话号码
    @Column(name = "tel_phone")
    private String telPhone;
	
	    //预计充值数量
    @Column(name = "planned_investment")
    private BigDecimal plannedInvestment;
	
	    //ETH充值地址
    @Column(name = "address")
    private String address;
	
	    //国家
    @Column(name = "country")
    private String country;
	
	    //证件类型
    @Column(name = "id_card_type")
    private String idCardType;
	
	    //证件照签名
    @Column(name = "front_side")
    private String frontSide;
	
	    //证件照后面
    @Column(name = "back_side")
    private String backSide;
	
	    //是否通过
    @Column(name = "check_status")
    private Integer checkStatus;
	
	    //充值地址（通过审核后生成）
    @Column(name = "deposit_address")
    private String depositAddress;
	

	/**
	 * 设置：
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取：
	 */
	public String getId() {
		return id;
	}
	/**
	 * 设置：
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * 获取：
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * 设置：
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	/**
	 * 获取：
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * 设置：
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	/**
	 * 获取：
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * 设置：生日
	 */
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	/**
	 * 获取：生日
	 */
	public String getBirthday() {
		return birthday;
	}
	/**
	 * 设置：邮箱
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * 获取：邮箱
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * 设置：国际区号
	 */
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	/**
	 * 获取：国际区号
	 */
	public String getAreaCode() {
		return areaCode;
	}
	/**
	 * 设置：电话号码
	 */
	public void setTelPhone(String telPhone) {
		this.telPhone = telPhone;
	}
	/**
	 * 获取：电话号码
	 */
	public String getTelPhone() {
		return telPhone;
	}
	/**
	 * 设置：预计充值数量
	 */
	public void setPlannedInvestment(BigDecimal plannedInvestment) {
		this.plannedInvestment = plannedInvestment;
	}
	/**
	 * 获取：预计充值数量
	 */
	public BigDecimal getPlannedInvestment() {
		return plannedInvestment;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * 设置：国家
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	/**
	 * 获取：国家
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * 设置：证件类型
	 */
	public void setIdCardType(String idCardType) {
		this.idCardType = idCardType;
	}
	/**
	 * 获取：证件类型
	 */
	public String getIdCardType() {
		return idCardType;
	}
	/**
	 * 设置：证件照签名
	 */
	public void setFrontSide(String frontSide) {
		this.frontSide = frontSide;
	}
	/**
	 * 获取：证件照签名
	 */
	public String getFrontSide() {
		return frontSide;
	}
	/**
	 * 设置：证件照后面
	 */
	public void setBackSide(String backSide) {
		this.backSide = backSide;
	}
	/**
	 * 获取：证件照后面
	 */
	public String getBackSide() {
		return backSide;
	}
	/**
	 * 设置：充值地址（通过审核后生成）
	 */
	public void setDepositAddress(String depositAddress) {
		this.depositAddress = depositAddress;
	}
	/**
	 * 获取：充值地址（通过审核后生成）
	 */
	public String getDepositAddress() {
		return depositAddress;
	}
}
