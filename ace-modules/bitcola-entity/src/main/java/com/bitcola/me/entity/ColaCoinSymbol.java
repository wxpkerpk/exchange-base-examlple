package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 交易对
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Data
@Table(name = "ag_admin_v1.cola_coin_symbol")
public class ColaCoinSymbol implements Serializable {
    private static final long serialVersionUID = 1L;

    //
    @Id
    private String id;

    //币种
    @Column(name = "coin_code")
    private String coinCode;

    //交易介质（USDT,BTC,ETH）
    @Column(name = "symbol")
    private String symbol;

    //交易费率
    @Column(name = "fees")
    private BigDecimal fees;

    @Column(name = "sort")
    private Integer sort;
    @Column(name = "online_time")
    private Long onlineTime;

    /**
     * 单笔最小限额
     */
    private BigDecimal min;
    /**
     * 单笔最大限额
     */
    private BigDecimal max;

    public Long getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(Long onlineTime) {
        this.onlineTime = onlineTime;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    String icon;

    @Column(name = "amount_scale")
    private Integer amountScale;
    @Column(name = "price_scale")
    private Integer priceScale;

    public Integer getAmountScale() {
        return amountScale;
    }

    public void setAmountScale(Integer amountScale) {
        this.amountScale = amountScale;
    }

    public Integer getPriceScale() {
        return priceScale;
    }

    public void setPriceScale(Integer priceScale) {
        this.priceScale = priceScale;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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
     * 设置：交易介质（USDT,BTC,ETH）
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 获取：交易介质（USDT,BTC,ETH）
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * 设置：交易费率
     */
    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    /**
     * 获取：交易费率
     */
    public BigDecimal getFees() {
        return fees;
    }
}
