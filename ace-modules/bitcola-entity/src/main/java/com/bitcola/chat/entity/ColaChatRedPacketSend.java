package com.bitcola.chat.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;


/**
 * 发红包表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-08 19:29:52
 */
@Table(name = "ag_admin_v1.cola_chat_red_packet_send")
public class ColaChatRedPacketSend implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //发送者 ID
    @Column(name = "user_id")
    private String userId;
	
	    //发送总数量
    @Column(name = "number")
    private Integer number;
	
	    //发送总金额
    @Column(name = "amount")
    private BigDecimal amount;
	
	    //剩余金额
    @Column(name = "remain")
    private BigDecimal remain;
	
	    //红包发送时间
    @Column(name = "send_date")
    private Long sendDate;
	
	    //最后一人抢红包时间
    @Column(name = "last_date")
    private Long lastDate;
	
	    //祝福语
    @Column(name = "wish")
    private String wish;
	
	    //红包币种
    @Column(name = "coin_code")
    private String coinCode;
	
	    //剩余个数
    @Column(name = "remain_number")
    private Integer remainNumber;
	
	    //类型普通红包，拼手气红包（NORMAL,RANDOM）
    @Column(name = "type")
    private String type;
	
	    //1此红包已经完成，抢完或者退回
    @Column(name = "complete")
    private Integer complete;
	
	    //接受群 ID
    @Column(name = "to_group")
    private String toGroup;

	    //接收人 ID
    @Column(name = "to_user")
    private String toUser;


	public String getToGroup() {
		return toGroup;
	}

	public void setToGroup(String toGroup) {
		this.toGroup = toGroup;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
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
	 * 设置：发送者 ID
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * 获取：发送者 ID
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * 设置：发送总数量
	 */
	public void setNumber(Integer number) {
		this.number = number;
	}
	/**
	 * 获取：发送总数量
	 */
	public Integer getNumber() {
		return number;
	}
	/**
	 * 设置：发送总金额
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	/**
	 * 获取：发送总金额
	 */
	public BigDecimal getAmount() {
		return amount;
	}
	/**
	 * 设置：剩余金额
	 */
	public void setRemain(BigDecimal remain) {
		this.remain = remain;
	}
	/**
	 * 获取：剩余金额
	 */
	public BigDecimal getRemain() {
		return remain;
	}
	/**
	 * 设置：红包发送时间
	 */
	public void setSendDate(Long sendDate) {
		this.sendDate = sendDate;
	}
	/**
	 * 获取：红包发送时间
	 */
	public Long getSendDate() {
		return sendDate;
	}
	/**
	 * 设置：最后一人抢红包时间
	 */
	public void setLastDate(Long lastDate) {
		this.lastDate = lastDate;
	}
	/**
	 * 获取：最后一人抢红包时间
	 */
	public Long getLastDate() {
		return lastDate;
	}
	/**
	 * 设置：祝福语
	 */
	public void setWish(String wish) {
		this.wish = wish;
	}
	/**
	 * 获取：祝福语
	 */
	public String getWish() {
		return wish;
	}
	/**
	 * 设置：红包币种
	 */
	public void setCoinCode(String coinCode) {
		this.coinCode = coinCode;
	}
	/**
	 * 获取：红包币种
	 */
	public String getCoinCode() {
		return coinCode;
	}
	/**
	 * 设置：剩余个数
	 */
	public void setRemainNumber(Integer remainNumber) {
		this.remainNumber = remainNumber;
	}
	/**
	 * 获取：剩余个数
	 */
	public Integer getRemainNumber() {
		return remainNumber;
	}
	/**
	 * 设置：类型普通红包，拼手气红包（NORMAL,RANDOM）
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * 获取：类型普通红包，拼手气红包（NORMAL,RANDOM）
	 */
	public String getType() {
		return type;
	}
	/**
	 * 设置：1此红包已经完成，抢完或者退回
	 */
	public void setComplete(Integer complete) {
		this.complete = complete;
	}
	/**
	 * 获取：1此红包已经完成，抢完或者退回
	 */
	public Integer getComplete() {
		return complete;
	}
}
