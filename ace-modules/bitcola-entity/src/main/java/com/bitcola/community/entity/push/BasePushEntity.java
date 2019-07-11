package com.bitcola.community.entity.push;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zkq
 * @create 2018-11-15 10:16
 **/
@Data
public class BasePushEntity implements Serializable {
    Integer type;
    String title;
    String desc;
    Object data;
}
