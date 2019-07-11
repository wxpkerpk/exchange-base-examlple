package com.bitcola.me.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 用户提现记录
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Data
@Table(name = "ag_admin_v1.cola_me_balance_withdrawin")
public class ColaMeBalanceWithdrawin implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;

	@Column(name = "tx_id")
    private String txId;
	@Column(name = "fees")
    private BigDecimal fees;

	@Column(name = "confirmations")
    private Integer confirmations;

	@Column(name = "confirmation_number")
    private Integer confirmationNumber;

	@Column(name = "icon")
	private String icon;
	    //
    @Column(name = "user_id")
    private String userId;
	
	    //币种
    @Column(name = "coin_code")
    private String coinCode;
	
	    //重提数量
    @Column(name = "number")
    private BigDecimal number;
	    //实际提现数量
    @Column(name = "real_number")
    private BigDecimal realNumber;

	    //类型，冲提（IN,WITHDRAW）
    @Column(name = "type")
    private String type;
	
	    //冲提地址
    @Column(name = "address")
    private String address;
	
	    //备注
    @Column(name = "note")
    private String note;
	
	    //提币状态
    @Column(name = "status")
    private String status;
	
	    //日期
    @Column(name = "date")
    private Long date;
    @Column(name = "reason")
    private String reason;
    private String sign;
    @Column(name = "audit_reason")
    private String auditReason;


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
	 * 设置：重提数量
	 */
	public void setNumber(BigDecimal number) {
		this.number = number;
	}
	/**
	 * 获取：重提数量
	 */
	public BigDecimal getNumber() {
		return number;
	}
	/**
	 * 设置：类型，冲提（IN,WITHDRAW）
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * 获取：类型，冲提（IN,WITHDRAW）
	 */
	public String getType() {
		return type;
	}
	/**
	 * 设置：冲提地址
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * 获取：冲提地址
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
	 * 设置：提币状态
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * 获取：提币状态
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * 设置：日期
	 */
	public void setDate(Long date) {
		this.date = date;
	}
	/**
	 * 获取：日期
	 */
	public Long getDate() {
		return date;
	}

	public BigDecimal getRealNumber() {
		return realNumber;
	}

	public void setRealNumber(BigDecimal realNumber) {
		this.realNumber = realNumber;
	}
}
