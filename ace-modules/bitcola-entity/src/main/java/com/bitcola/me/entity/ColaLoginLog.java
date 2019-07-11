package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * 登录日志表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-30 16:19:50
 */
@Data
@Table(name = "ag_admin_v1.cola_login_log")
public class ColaLoginLog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //用户 ID
    @Column(name = "user_id")
    private String userId;
	
	    //用户名
    @Column(name = "username")
    private String username;
	
	    //昵称
    @Column(name = "nick_name")
    private String nickName;
	
	    //登录日期
    @Column(name = "time")
    private Long time;
	
	    //平台
    @Column(name = "platform")
    private String platform;
	
	    //设备
    @Column(name = "device")
    private String device;
	
	    //IP地址
    @Column(name = "ip")
    private String ip;
	
	    //地区
    @Column(name = "area")
    private String area;

    @Column(name = "status")
    private String status;

    @Column(name = "version")
    private String version;
	

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
	 * 设置：用户名
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * 获取：用户名
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * 设置：昵称
	 */
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	/**
	 * 获取：昵称
	 */
	public String getNickName() {
		return nickName;
	}
	/**
	 * 设置：登录日期
	 */
	public void setTime(Long time) {
		this.time = time;
	}
	/**
	 * 获取：登录日期
	 */
	public Long getTime() {
		return time;
	}
	/**
	 * 设置：平台
	 */
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	/**
	 * 获取：平台
	 */
	public String getPlatform() {
		return platform;
	}
	/**
	 * 设置：设备
	 */
	public void setDevice(String device) {
		this.device = device;
	}
	/**
	 * 获取：设备
	 */
	public String getDevice() {
		return device;
	}
	/**
	 * 设置：IP地址
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
	/**
	 * 获取：IP地址
	 */
	public String getIp() {
		return ip;
	}
	/**
	 * 设置：地区
	 */
	public void setArea(String area) {
		this.area = area;
	}
	/**
	 * 获取：地区
	 */
	public String getArea() {
		return area;
	}
}
