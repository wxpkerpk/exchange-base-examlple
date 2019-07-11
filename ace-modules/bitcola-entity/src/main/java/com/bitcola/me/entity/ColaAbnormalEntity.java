package com.bitcola.me.entity;

import lombok.Data;

/**
 * 异常
 *
 * @author zkq
 * @create 2018-11-22 18:20
 **/
@Data
public class ColaAbnormalEntity {

    String id;
    String recordId;
    Long time;
    String userId;
    String reason;
    String status;
    String result;


}
