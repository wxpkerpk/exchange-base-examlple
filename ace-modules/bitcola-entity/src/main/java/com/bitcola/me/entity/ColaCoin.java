package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 币种表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Data
@Table(name = "ag_admin_v1.cola_coin")
public class ColaCoin implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;

    @Column(name = "min_withdraw_number")
    private BigDecimal minWithdrawNumber;
    @Column(name = "deposit_confirmation_number")
    private Integer depositConfirmationNumber;
    @Column(name = "confirmation_number")
    private Integer confirmationNumber;
	private Integer sort;
	@Column(name = "cny_scale")
	Integer cnyScale;
	@Column(name = "usd_scale")
	Integer usdScale;
	@Column(name = "jpa_scale")
	Integer jpyScale;
	@Column(name = "eur_scale")
	Integer eurScale;
	@Column(name = "gbp_scale")
	Integer gbpScale;
	String belong;
	@Column(name = "deposit_min")
	BigDecimal depositMin;

	@Column(name = "fee_coin_code")
	private String feeCoinCode;
	    //
    @Column(name = "coin_code")
    private String coinCode;
	
	    //币种中文名
    @Column(name = "coin_code_cn")
    private String coinCodeCn;
	
	    //英文名
    @Column(name = "coin_code_en")
    private String coinCodeEn;
	
	    //简介
    @Column(name = "coin_note")
    private String coinNote;
	
	    //官方网站
    @Column(name = "website")
    private String website;
	
	    //是否需要备注，0false,1true
    @Column(name = "is_need_note")
    private Integer isNeedNote;
	
	    //提币百分比费率
    @Column(name = "fees_percent")
    private BigDecimal feesPercent;
	
	    //固定费率（总费率为 5% + 5）
    @Column(name = "fees_flat")
    private BigDecimal feesFlat;
	
	    //区块链浏览器
    @Column(name = "block_browser")
    private String blockBrowser;
	
	    //是否可提币 1表示是 ，0 表示否
    @Column(name = "is_withdraw")
    private Integer isWithdraw;
	
	    //是否可冲币 1表示是 ，0 表示否
    @Column(name = "is_recharge")
    private Integer isRecharge;
	
	    //是否是 ERC20币种 1表示是 ，0 表示否
    @Column(name = "is_erc20")
    private Integer isErc20;
	
	    //提现默认单笔限额
    @Column(name = "withdraw_one")
    private BigDecimal withdrawOne;
	
	    //提现默认当日限额
    @Column(name = "withdraw_amount")
    private BigDecimal withdrawAmount;
	
	    //自动提现额度，但是会受到当日提现总额，总次数限制
    @Column(name = "withdraw_auto")
    private BigDecimal withdrawAuto;
	
	    //真实提币所花费费率
    @Column(name = "fees_real")
    private BigDecimal feesReal;

	@Column(name = "icon")
    private String icon;

	@Column(name = "prec")
	private Integer prec;

	@Column(name = "recharge_description_cn")
	private String rechargeDescriptionCn;

	@Column(name = "recharge_description_en")
	private String rechargeDescriptionEn;


	public BigDecimal getMinWithdrawNumber() {
		return minWithdrawNumber;
	}

	public void setMinWithdrawNumber(BigDecimal minWithdrawNumber) {
		this.minWithdrawNumber = minWithdrawNumber;
	}

	public Integer getDepositConfirmationNumber() {
		return depositConfirmationNumber;
	}

	public void setDepositConfirmationNumber(Integer depositConfirmationNumber) {
		this.depositConfirmationNumber = depositConfirmationNumber;
	}

	public Integer getConfirmationNumber() {
		return confirmationNumber;
	}

	public void setConfirmationNumber(Integer confirmationNumber) {
		this.confirmationNumber = confirmationNumber;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Integer getPrec() {
		return prec;
	}

	public void setPrec(Integer prec) {
		this.prec = prec;
	}

	public String getRechargeDescriptionCn() {
		return rechargeDescriptionCn;
	}

	public void setRechargeDescriptionCn(String rechargeDescriptionCn) {
		this.rechargeDescriptionCn = rechargeDescriptionCn;
	}

	public String getRechargeDescriptionEn() {
		return rechargeDescriptionEn;
	}

	public void setRechargeDescriptionEn(String rechargeDescriptionEn) {
		this.rechargeDescriptionEn = rechargeDescriptionEn;
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
	 * 设置：
	 */
	public void setCoinCode(String coinCode) {
		this.coinCode = coinCode;
	}
	/**
	 * 获取：
	 */
	public String getCoinCode() {
		return coinCode;
	}
	/**
	 * 设置：币种中文名
	 */
	public void setCoinCodeCn(String coinCodeCn) {
		this.coinCodeCn = coinCodeCn;
	}
	/**
	 * 获取：币种中文名
	 */
	public String getCoinCodeCn() {
		return coinCodeCn;
	}
	/**
	 * 设置：英文名
	 */
	public void setCoinCodeEn(String coinCodeEn) {
		this.coinCodeEn = coinCodeEn;
	}
	/**
	 * 获取：英文名
	 */
	public String getCoinCodeEn() {
		return coinCodeEn;
	}
	/**
	 * 设置：简介
	 */
	public void setCoinNote(String coinNote) {
		this.coinNote = coinNote;
	}
	/**
	 * 获取：简介
	 */
	public String getCoinNote() {
		return coinNote;
	}
	/**
	 * 设置：官方网站
	 */
	public void setWebsite(String website) {
		this.website = website;
	}
	/**
	 * 获取：官方网站
	 */
	public String getWebsite() {
		return website;
	}
	/**
	 * 设置：是否需要备注，0false,1true
	 */
	public void setIsNeedNote(Integer isNeedNote) {
		this.isNeedNote = isNeedNote;
	}
	/**
	 * 获取：是否需要备注，0false,1true
	 */
	public Integer getIsNeedNote() {
		return isNeedNote;
	}
	/**
	 * 设置：提币百分比费率
	 */
	public void setFeesPercent(BigDecimal feesPercent) {
		this.feesPercent = feesPercent;
	}
	/**
	 * 获取：提币百分比费率
	 */
	public BigDecimal getFeesPercent() {
		return feesPercent;
	}
	/**
	 * 设置：固定费率（总费率为 5% + 5）
	 */
	public void setFeesFlat(BigDecimal feesFlat) {
		this.feesFlat = feesFlat;
	}
	/**
	 * 获取：固定费率（总费率为 5% + 5）
	 */
	public BigDecimal getFeesFlat() {
		return feesFlat;
	}
	/**
	 * 设置：区块链浏览器
	 */
	public void setBlockBrowser(String blockBrowser) {
		this.blockBrowser = blockBrowser;
	}
	/**
	 * 获取：区块链浏览器
	 */
	public String getBlockBrowser() {
		return blockBrowser;
	}
	/**
	 * 设置：是否可提币 1表示是 ，0 表示否
	 */
	public void setIsWithdraw(Integer isWithdraw) {
		this.isWithdraw = isWithdraw;
	}
	/**
	 * 获取：是否可提币 1表示是 ，0 表示否
	 */
	public Integer getIsWithdraw() {
		return isWithdraw;
	}
	/**
	 * 设置：是否可冲币 1表示是 ，0 表示否
	 */
	public void setIsRecharge(Integer isRecharge) {
		this.isRecharge = isRecharge;
	}
	/**
	 * 获取：是否可冲币 1表示是 ，0 表示否
	 */
	public Integer getIsRecharge() {
		return isRecharge;
	}
	/**
	 * 设置：是否是 ERC20币种 1表示是 ，0 表示否
	 */
	public void setIsErc20(Integer isErc20) {
		this.isErc20 = isErc20;
	}
	/**
	 * 获取：是否是 ERC20币种 1表示是 ，0 表示否
	 */
	public Integer getIsErc20() {
		return isErc20;
	}
	/**
	 * 设置：提现默认单笔限额
	 */
	public void setWithdrawOne(BigDecimal withdrawOne) {
		this.withdrawOne = withdrawOne;
	}
	/**
	 * 获取：提现默认单笔限额
	 */
	public BigDecimal getWithdrawOne() {
		return withdrawOne;
	}
	/**
	 * 设置：提现默认当日限额
	 */
	public void setWithdrawAmount(BigDecimal withdrawAmount) {
		this.withdrawAmount = withdrawAmount;
	}
	/**
	 * 获取：提现默认当日限额
	 */
	public BigDecimal getWithdrawAmount() {
		return withdrawAmount;
	}
	/**
	 * 设置：自动提现额度，但是会受到当日提现总额，总次数限制
	 */
	public void setWithdrawAuto(BigDecimal withdrawAuto) {
		this.withdrawAuto = withdrawAuto;
	}
	/**
	 * 获取：自动提现额度，但是会受到当日提现总额，总次数限制
	 */
	public BigDecimal getWithdrawAuto() {
		return withdrawAuto;
	}
	/**
	 * 设置：真实提币所花费费率
	 */
	public void setFeesReal(BigDecimal feesReal) {
		this.feesReal = feesReal;
	}
	/**
	 * 获取：真实提币所花费费率
	 */
	public BigDecimal getFeesReal() {
		return feesReal;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
