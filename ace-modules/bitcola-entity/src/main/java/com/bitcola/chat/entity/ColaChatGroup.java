package com.bitcola.chat.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;


/**
 * 群组表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-08 09:19:59
 */
@Data
@Table(name = "ag_admin_v1.cola_chat_group")
public class ColaChatGroup implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //分组名称
    @Column(name = "group_name")
    private String groupName;
	
	    //图标
    @Column(name = "avatar")
    private String avatar;
	
	    //简介
    @Column(name = "comment")
    private String comment;
	

	    //限制人数
    @Column(name = "restricts")
    private Integer restricts;

	/**
	 * 加群方式
	 */
	@Column(name = "join_mode")
	private String joinMode;

	public Integer getRestricts() {
		return restricts;
	}

	public void setRestricts(Integer restricts) {
		this.restricts = restricts;
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
	 * 设置：分组名称
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	/**
	 * 获取：分组名称
	 */
	public String getGroupName() {
		return groupName;
	}
	/**
	 * 设置：图标
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	/**
	 * 获取：图标
	 */
	public String getAvatar() {
		return avatar;
	}
	/**
	 * 设置：简介
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * 获取：简介
	 */
	public String getComment() {
		return comment;
	}
}
