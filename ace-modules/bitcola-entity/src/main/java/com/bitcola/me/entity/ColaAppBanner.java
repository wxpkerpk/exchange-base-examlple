package com.bitcola.me.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-03-20 10:59
 **/
@Data
@Table(name = "ag_admin_v1.cola_app_banner")
public class ColaAppBanner {

    @Id
    String id;
    String banner;
    String url;
    @JSONField(serialize = false)
    @Column(name = "banner_cn")
    String bannerCn;
    @JSONField(serialize = false)
    @Column(name = "url_cn")
    String urlCn;
}
