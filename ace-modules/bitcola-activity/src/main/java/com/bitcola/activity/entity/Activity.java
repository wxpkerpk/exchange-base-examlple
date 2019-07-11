package com.bitcola.activity.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-11-30 10:50
 **/
@Data
public class Activity {
    @Id
    String id;
    @Column(name = "user_id")
    String userId;
    BigDecimal number;
    Long timestamp;
}
