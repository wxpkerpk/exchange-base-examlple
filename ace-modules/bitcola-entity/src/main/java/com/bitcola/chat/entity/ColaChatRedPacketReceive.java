package com.bitcola.chat.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;


/**
 * 抢红包表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-08 19:33:28
 */
@Table(name = "ag_admin_v1.cola_chat_red_packet_receive")
public class ColaChatRedPacketReceive implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //红包 ID
    @Column(name = "red_packet_id")
    private String redPacketId;
	
	    //用户 ID
    @Column(name = "user_id")
    private String userId;
	
	    //金额
    @Column(name = "money")
    private BigDecimal money;
	
	    //币种
    @Column(name = "coin_code")
    private String coinCode;
	
	    //抢红包时间
    @Column(name = "date")
    private Long date;
	
	    //1为手气最佳，否则为0
    @Column(name = "is_max")
    private Integer isMax;
	

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
	 * 设置：红包 ID
	 */
	public void setRedPacketId(String redPacketId) {
		this.redPacketId = redPacketId;
	}
	/**
	 * 获取：红包 ID
	 */
	public String getRedPacketId() {
		return redPacketId;
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
	 * 设置：金额
	 */
	public void setMoney(BigDecimal money) {
		this.money = money;
	}
	/**
	 * 获取：金额
	 */
	public BigDecimal getMoney() {
		return money;
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
	 * 设置：抢红包时间
	 */
	public void setDate(Long date) {
		this.date = date;
	}
	/**
	 * 获取：抢红包时间
	 */
	public Long getDate() {
		return date;
	}
	/**
	 * 设置：1为手气最佳，否则为0
	 */
	public void setIsMax(Integer isMax) {
		this.isMax = isMax;
	}
	/**
	 * 获取：1为手气最佳，否则为0
	 */
	public Integer getIsMax() {
		return isMax;
	}
}
