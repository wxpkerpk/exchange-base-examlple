package com.bitcola.me.entity;

import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-12 19:12:16
 */
@Table(name = "ag_admin_v1.cola_me_work_order_detail")
public class ColaMeWorkOrderDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	
	    //
    @Id
    private String id;
	
	    //工单 ID
    @Column(name = "order_id")
    private String orderId;
	
	    //发表时间
    @Column(name = "time")
    private Long time;
	
	    //内容
    @Column(name = "content")
    private String content;

	@Column(name = "images")
    private String images;

	@Transient
	private List<Object> imageList = new ArrayList<>();
	    //用户 ID
    @Column(name = "user_id")
    private String userId;
	
	    //用户名
    @Column(name = "username")
    private String username;

    @Column(name = "nick_name")
    private String nickName;

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
	 * 设置：工单 ID
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	/**
	 * 获取：工单 ID
	 */
	public String getOrderId() {
		return orderId;
	}
	/**
	 * 设置：发表时间
	 */
	public void setTime(Long time) {
		this.time = time;
	}
	/**
	 * 获取：发表时间
	 */
	public Long getTime() {
		return time;
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
	 * 设置：截图，最多3张
	 */
	public void setImages(String images) {
		if (!StringUtils.isEmpty(images)){
			imageList.addAll(Arrays.asList(images.split(",")));
		}
		this.images = images;
	}
	/**
	 * 获取：截图，最多3张
	 */
	public String getImages() {
		if (StringUtils.isEmpty(images) && imageList.size()!=0){
			images = "";
			for (Object s : imageList) {
				images += ","+s;
			}
			images = images.replaceFirst(",","");
		}
		return images;
	}

	public List<Object> getImageList() {
		if (imageList.size() == 0 && !StringUtils.isEmpty(images)){
			imageList.addAll(Arrays.asList(images.split(",")));
		}
		return imageList;
	}

	public void setImageList(List<Object> imageList) {
		if (imageList.size()!=0){
			images = "";
			for (Object s : imageList) {
				images += ","+s;
			}
			images = images.replaceFirst(",","");
		}
		this.imageList = imageList;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
}
