package com.bitcola.community.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 打赏
 *
 * @author zkq
 * @create 2018-10-31 17:10
 **/
@Data
public class DonateEntity implements Serializable {

    @Id
    String id;
    @Column(name = "user_id")
    String userId;
    String type;
    BigDecimal number;
    Long time;
    @Column(name = "coin_code")
    String coinCode;

}
