package com.bitcola.me.entity;

import com.alibaba.fastjson.annotation.JSONField;
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
 * @date 2018-09-12 19:12:17
 */
@Table(name = "ag_admin_v1.cola_me_work_order")
public class ColaMeWorkOrder implements Serializable {
	private static final long serialVersionUID = 1L;

	    //
    @Id
    private String id;
	
	    //标题
    @Column(name = "title")
    private String title;
	
	    //内容
	@JSONField(serialize = false)
    @Column(name = "content")
    private String content;
	
	    //创建时间
    @Column(name = "time")
    private Long time;
	
	    //用户 id
    @Column(name = "from_user_id")
    private String fromUserId;
	
	    //用户名
    @Column(name = "from_username")
    private String fromUsername;
	    //用户名
    @Column(name = "from_nick_name")
    private String fromNickName;

    @Column(name = "tx_id")
    private String txId;
	
	    //"已提交" or "已回复" or "已完成"
    @Column(name = "status")
    private String status;
	
	    //“充值” or "提现" or "其他"
    @Column(name = "type")
    private String type;
	
	    //币种
    @Column(name = "coin_code")
    private String coinCode;
	
	    //截图，最多3张
    @Column(name = "images")
    private String images;

	@Transient
    private List imageList = new ArrayList<>();
	

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
	 * 设置：标题
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * 获取：标题
	 */
	public String getTitle() {
		return title;
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
	 * 设置：创建时间
	 */
	public void setTime(Long time) {
		this.time = time;
	}
	/**
	 * 获取：创建时间
	 */
	public Long getTime() {
		return time;
	}
	/**
	 * 设置：用户 id
	 */
	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}
	/**
	 * 获取：用户 id
	 */
	public String getFromUserId() {
		return fromUserId;
	}
	/**
	 * 设置：用户名
	 */
	public void setFromUsername(String fromUsername) {
		this.fromUsername = fromUsername;
	}
	/**
	 * 获取：用户名
	 */
	public String getFromUsername() {
		return fromUsername;
	}
	/**
	 * 设置："已提交" or "已回复" or "已完成"
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * 获取："已提交" or "已回复" or "已完成"
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * 设置：“充值” or "提现" or "其他"
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * 获取：“充值” or "提现" or "其他"
	 */
	public String getType() {
		return type;
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

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
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

	public String getFromNickName() {
		return fromNickName;
	}

	public void setFromNickName(String fromNickName) {
		this.fromNickName = fromNickName;
	}
}
