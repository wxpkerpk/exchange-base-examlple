package com.bitcola.caculate.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/*
 * @author:wx
 * @description:订单
 * @create:2018-07-31  19:58
 */
@Table(name = "ag_admin_v1.cola_caculate_order")
@Data
public class ColaOrder implements Serializable {
    @Column(name ="id")
    String id;
    @Column(name ="count")
    BigDecimal count=BigDecimal.ZERO;
    @Column(name ="coin_code")
    String coinCode;
    @Column(name ="price")
    BigDecimal price;
    @Column(name ="user_id")
    String userId;

    @Column(name="total")
    BigDecimal total=BigDecimal.ZERO;

    @Column(name = "origin_total")
    BigDecimal originTotal=BigDecimal.ZERO;

    @Column(name = "status")
    String status;



    public ColaOrder() {
    }


    public ColaOrder(ColaOrder colaOrder,BigDecimal count,BigDecimal price) {
        this.id = colaOrder.getId();
        this.count = count;

        this.coinCode = colaOrder.getCoinCode();
        this.price = price;
        this.userId = colaOrder.getUserId();
        this.type = colaOrder.getType();
        this.time = colaOrder.getTime();
    }

    String type;




    long time=0;


}
