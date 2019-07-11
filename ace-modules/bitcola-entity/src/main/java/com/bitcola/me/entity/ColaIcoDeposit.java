package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 充值详情表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-29 14:43:56
 */
@Data
@Table(name = "ag_admin_v1.cola_ico_deposit")
public class ColaIcoDeposit implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //
    @Column(name = "user_id")
    private String userId;
	
	    //充值ETH数量
    @Column(name = "deposit_number")
    private BigDecimal depositNumber;
	
	    //充值时 ETH_USDT价格
    @Column(name = "price")
    private BigDecimal price;
	
	    //token数量
    @Column(name = "cola_token_number")
    private BigDecimal colaTokenNumber;
	
	    //时间
    @Column(name = "time")
    private Long time;
	
	    //已发放数量
    @Column(name = "grant_number")
    private BigDecimal grantNumber;
	
	    //未发放数量
    @Column(name = "not_grant_number")
    private BigDecimal notGrantNumber;
	
	    //充值奖励数量
    @Column(name = "bonus_number")
    private BigDecimal bonusNumber;

	@Column(name = "confirm_status")
    private String confirmStatus;

	@Column(name = "confirm_number")
    private Integer confirmNumber;

	@Column(name = "current_confirm_number")
    private Integer currentConfirmNumber;

	private String fromAddress;
	private String toAddress;

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
	 * 设置：充值ETH数量
	 */
	public void setDepositNumber(BigDecimal depositNumber) {
		this.depositNumber = depositNumber;
	}
	/**
	 * 获取：充值ETH数量
	 */
	public BigDecimal getDepositNumber() {
		return depositNumber;
	}

	/**
	 * 设置：token数量
	 */
	public void setColaTokenNumber(BigDecimal colaTokenNumber) {
		this.colaTokenNumber = colaTokenNumber;
	}
	/**
	 * 获取：token数量
	 */
	public BigDecimal getColaTokenNumber() {
		return colaTokenNumber;
	}
	/**
	 * 设置：时间
	 */
	public void setTime(Long time) {
		this.time = time;
	}
	/**
	 * 获取：时间
	 */
	public Long getTime() {
		return time;
	}
	/**
	 * 设置：已发放数量
	 */
	public void setGrantNumber(BigDecimal grantNumber) {
		this.grantNumber = grantNumber;
	}
	/**
	 * 获取：已发放数量
	 */
	public BigDecimal getGrantNumber() {
		return grantNumber;
	}
	/**
	 * 设置：未发放数量
	 */
	public void setNotGrantNumber(BigDecimal notGrantNumber) {
		this.notGrantNumber = notGrantNumber;
	}
	/**
	 * 获取：未发放数量
	 */
	public BigDecimal getNotGrantNumber() {
		return notGrantNumber;
	}

	public BigDecimal getBonusNumber() {
		return bonusNumber;
	}

	public void setBonusNumber(BigDecimal bonusNumber) {
		this.bonusNumber = bonusNumber;
	}
}
