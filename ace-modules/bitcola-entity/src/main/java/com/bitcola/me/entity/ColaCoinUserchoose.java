package com.bitcola.me.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;


/**
 * 用户自选表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Table(name = "ag_admin_v1.cola_coin_userchoose")
public class ColaCoinUserchoose implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //交易介质
    @Column(name = "symbol")
    private String symbol;
	
	    //币种
    @Column(name = "coin_code")
    private String coinCode;

    @Column(name = "user_id")
	@JSONField(serialize = false)
    private String userId;

    @Transient
    private String pair;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}


    public String getPair() {
	    if (StringUtils.isEmpty(pair)){
	        pair = coinCode+"_"+symbol;
        }
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

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
	 * 设置：交易介质
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	/**
	 * 获取：交易介质
	 */
	public String getSymbol() {
		return symbol;
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
}
