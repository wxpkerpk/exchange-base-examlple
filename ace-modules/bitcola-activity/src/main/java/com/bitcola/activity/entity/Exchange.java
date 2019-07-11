package com.bitcola.activity.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2018-12-26 21:51
 **/
@Table(name = "ag_admin_v1.cola_activity_exchange")
@Data
public class Exchange extends Activity {

    @Column(name = "coin_code")
    String coinCode;
    String description;

}
