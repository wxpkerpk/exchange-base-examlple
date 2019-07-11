package com.bitcola.exchange.security.community.vo;

import lombok.Data;

import java.util.List;

/**
 * @author lky
 * @create 2019-05-05 14:25
 **/
@Data
public class ReviewStateVo {
    private String id;
    private List<String> tag;
    private String reviewType;

}
