package com.bitcola.me.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 用户钱包
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Table(name = "ag_admin_v1.cola_me_balance")
public class ColaMeBalance implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //用户 ID
    @Column(name = "user_id")
    private String userId;
	
	    //币种
    @Column(name = "coin_code")
    private String coinCode;
	
	    //可用金额
    @Column(name = "balance_available")
    private BigDecimal balanceAvailable;
	
	    //冻结金额
    @Column(name = "balance_frozen")
    private BigDecimal balanceFrozen;
	
	    //充值地址
    @Column(name = "address_in")
    private String addressIn;
	
	    //备注（用于充值）
    @Column(name = "note")
    private String note;


	/**
	 * 设置：
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取：
	 */
	@JsonIgnore
	public String getId() {
		return id;
	}
	/**
	 * 设置：用户 ID
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * 获取：用户 ID
	 */
	@JsonIgnore
	public String getUserId() {
		return userId;
	}
	/**
	 * 设置：币种
	 */
	public void setCoinCode(String coinCode) {
		this.coinCode = coinCode;
	}
	/**
	 * 获取：币种
	 */
	public String getCoinCode() {
		return coinCode;
	}
	/**
	 * 设置：可用金额
	 */
	public void setBalanceAvailable(BigDecimal balanceAvailable) {
		this.balanceAvailable = balanceAvailable;
	}
	/**
	 * 获取：可用金额
	 */
	public BigDecimal getBalanceAvailable() {
		return balanceAvailable;
	}
	/**
	 * 设置：冻结金额
	 */
	public void setBalanceFrozen(BigDecimal balanceFrozen) {
		this.balanceFrozen = balanceFrozen;
	}
	/**
	 * 获取：冻结金额
	 */
	public BigDecimal getBalanceFrozen() {
		return balanceFrozen;
	}
	/**
	 * 设置：充值地址
	 */
	public void setAddressIn(String addressIn) {
		this.addressIn = addressIn;
	}
	/**
	 * 获取：充值地址
	 */
	public String getAddressIn() {
		return addressIn;
	}
	/**
	 * 设置：备注（用于充值）
	 */
	public void setNote(String note) {
		this.note = note;
	}
	/**
	 * 获取：备注（用于充值）
	 */
	public String getNote() {
		return note;
	}
}
