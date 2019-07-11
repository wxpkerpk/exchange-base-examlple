package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * 用户历史提现地址
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-04 17:20:06
 */
@Data
@Table(name = "ag_admin_v1.cola_me_balance_withdraw_address")
public class ColaMeBalanceWithdrawAddress implements Serializable {
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
	
	    //地址
    @Column(name = "address")
    private String address;
	
	    //备注
    @Column(name = "note")
    private String note;
	
	    //标签
    @Column(name = "label")
    private String label;

    private Long time;
	

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
	 * 设置：用户 ID
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * 获取：用户 ID
	 */
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
	 * 设置：地址
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * 获取：地址
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * 设置：备注
	 */
	public void setNote(String note) {
		this.note = note;
	}
	/**
	 * 获取：备注
	 */
	public String getNote() {
		return note;
	}
	/**
	 * 设置：标签
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * 获取：标签
	 */
	public String getLabel() {
		return label;
	}
}
