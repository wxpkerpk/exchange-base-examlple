package com.bitcola.exchange.security.community.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 资讯
 *
 * @author lky
 * @create 2019-04-23 17:37
 **/
@Data
public class NewsEntity extends NewsItemEntity implements Serializable {

    private String content;

    private String introduction;



}
