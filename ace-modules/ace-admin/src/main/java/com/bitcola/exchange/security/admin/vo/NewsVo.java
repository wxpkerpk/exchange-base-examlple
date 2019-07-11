package com.bitcola.exchange.security.admin.vo;

import lombok.Data;


/**
 * @author zkq
 * @create 2018-11-01 12:40
 **/
@Data
public class NewsVo {
    String id;
    String title;
    Long time;
    String userId;
    private String firstClass;
    private String secondClass;
    private String threadClass;
    private Integer helpful;
    private Integer unhelpful;

}
