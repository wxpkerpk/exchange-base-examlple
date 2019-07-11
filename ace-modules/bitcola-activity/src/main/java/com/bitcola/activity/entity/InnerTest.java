package com.bitcola.activity.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2018-11-30 10:46
 **/
@Data
@Table(name = "ag_admin_v1.cola_activity_inner_test")
public class InnerTest extends Activity {

    @Column(name = "order_id")
    String orderId;
}
