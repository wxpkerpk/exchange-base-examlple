package com.bitcola.chat.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;


/**
 * 用户群组对应表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-08 09:19:59
 */
@Data
@Table(name = "ag_admin_v1.cola_chat_user_group")
public class ColaChatUserGroup implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //
    @Column(name = "user_id")
    private String userId;
	
	    //
    @Column(name = "group_id")
    private String groupId;
	
	    //1超级管理员，2管理员，3普通成员
    @Column(name = "level")
    private Integer level = 3;
	
	    //黑名单，1为黑名单，0则不是
    @Column(name = "is_black")
    private Integer isBlack = 0;

    @Column(name = "ack_time")
    private Long ackTime;

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
	 * 设置：
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	/**
	 * 获取：
	 */
	public String getGroupId() {
		return groupId;
	}
	/**
	 * 设置：1超级管理员，2管理员，3普通成员
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}
	/**
	 * 获取：1超级管理员，2管理员，3普通成员
	 */
	public Integer getLevel() {
		return level;
	}
	/**
	 * 设置：黑名单，1为黑名单，0则不是
	 */
	public void setIsBlack(Integer isBlack) {
		this.isBlack = isBlack;
	}
	/**
	 * 获取：黑名单，1为黑名单，0则不是
	 */
	public Integer getIsBlack() {
		return isBlack;
	}
}
