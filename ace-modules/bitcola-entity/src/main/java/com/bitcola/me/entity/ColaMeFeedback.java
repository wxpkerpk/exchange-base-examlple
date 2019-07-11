package com.bitcola.me.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * 意见反馈表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Table(name = "ag_admin_v1.cola_me_feedback")
public class ColaMeFeedback implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //用户 ID
    @Column(name = "user_id")
    private String userId;
	
	    //内容
    @Column(name = "content")
    private String content;
	
	    //图片，以逗号分隔
    @Column(name = "images")
    private String images;
	
	    //联系方式
    @Column(name = "contact")
    private String contact;
	
	    //提交日期
    @Column(name = "date")
    private Long date;
	

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
	 * 设置：内容
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * 获取：内容
	 */
	public String getContent() {
		return content;
	}
	/**
	 * 设置：图片，以逗号分隔
	 */
	public void setImages(String images) {
		this.images = images;
	}
	/**
	 * 获取：图片，以逗号分隔
	 */
	public String getImages() {
		return images;
	}
	/**
	 * 设置：联系方式
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	/**
	 * 获取：联系方式
	 */
	public String getContact() {
		return contact;
	}
	/**
	 * 设置：提交日期
	 */
	public void setDate(Long date) {
		this.date = date;
	}
	/**
	 * 获取：提交日期
	 */
	public Long getDate() {
		return date;
	}
}
