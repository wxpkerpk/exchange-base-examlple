package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2018-12-25 09:59
 **/
@Data
@Table(name = "ag_admin_v1.cola_user_limit")
public class ColaUserLimit {

    @Id
    String id;
    @Column(name = "user_id")
    String userId;
    Long time;
    Integer type = 0;
    String module;
    String reason;

    public Long limitTime(){
        if (type!=0){
            return 253370736000000L;
        } else {
            return time;
        }
    }

}
