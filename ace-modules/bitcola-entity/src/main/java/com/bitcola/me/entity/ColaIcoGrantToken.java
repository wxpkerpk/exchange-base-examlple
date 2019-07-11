package com.bitcola.me.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * ICO token 发放表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-29 14:43:56
 */
@Table(name = "ag_admin_v1.cola_ico_grant_token")
public class ColaIcoGrantToken implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //
    @Column(name = "user_id")
    private String userId;
	
	    //发放数量
    @Column(name = "number")
    private BigDecimal number;
	
	    //
    @Column(name = "time")
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
	 * 设置：发放数量
	 */
	public void setNumber(BigDecimal number) {
		this.number = number;
	}
	/**
	 * 获取：发放数量
	 */
	public BigDecimal getNumber() {
		return number;
	}
	/**
	 * 设置：
	 */
	public void setTime(Long time) {
		this.time = time;
	}
	/**
	 * 获取：
	 */
	public Long getTime() {
		return time;
	}
}
