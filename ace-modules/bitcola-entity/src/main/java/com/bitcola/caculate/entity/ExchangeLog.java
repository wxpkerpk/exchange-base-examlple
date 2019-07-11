package com.bitcola.caculate.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
 * @author:wx
 * @description:交易记录
 * @create:2018-08-14  20:28
 */
@Data

@Table(name = "ag_admin_v1.cola_exchange_log")
public class ExchangeLog {

    @Id
    String id;

    @Column(name = "user_id")
    String fromUserId;
    @Column(name = "to_user_id")
    String toUserId;
    @Column(name = "from_order_id")
    String fromOrderId;
    @Column(name = "to_order_id")
    String toOrderId;
    @Column(name = "time")
    long time = 0;

    @Column(name = "code")
    String code;
    @Column(name = "type")
    String type;
    @Column(name = "price")
    BigDecimal price;
    @Column(name="from_count")
    BigDecimal fromCount;
    @Column(name="to_count")
    BigDecimal toCount;

    @Column(name="from_fee")
    BigDecimal fromFee=BigDecimal.ZERO;
    @Column(name="to_fee")
    BigDecimal toFee=BigDecimal.ZERO;
    @Column(name="from_fee_coin_code")
    String fromFeeCoinCode;
    @Column(name="to_fee_coin_code")
    String toFeeCoinCode;
    @Column(name="sign")
    String sign;





    @Column(name = "from_code")
    String fromCode;
    @Column(name = "to_code")
    String toCode;

    public ExchangeLog() {
    }

    public ExchangeLog(String id, String fromUserId, String toUserId, String fromOrderId, String toOrderId, long time, String code, String type, BigDecimal price, BigDecimal fromCount, BigDecimal toCount, BigDecimal fromFee, BigDecimal toFee, String fromFeeCoinCode, String toFeeCoinCode,  String fromCode, String toCode) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.fromOrderId = fromOrderId;
        this.toOrderId = toOrderId;
        this.time = time;
        this.code = code;
        this.type = type;
        this.price = price;
        this.fromCount = fromCount;
        this.toCount = toCount;
        this.fromFee = fromFee;
        this.toFee = toFee;
        this.fromFeeCoinCode = fromFeeCoinCode;
        this.toFeeCoinCode = toFeeCoinCode;
        this.fromCode = fromCode;
        this.toCode = toCode;
    }

    public Map<String,Object> toMap()
    {

        var map=new HashMap<String,Object>(12);
        map.put("time",time);
        map.put("id",id);
        map.put("type",type);
        map.put("fromUserId",fromUserId);
        map.put("toUserId",toUserId);
        map.put("price",price);
        String orderId=null;
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal count=BigDecimal.ZERO;
        String feeCoinCode = toFeeCoinCode;
        switch (type){
            case "buy":{
              orderId=toOrderId;
              count=fromCount;
              break;
            }
            case "sell":{
                orderId=fromOrderId;
                count=fromCount;
                break;
            }
        }
        map.put("orderId",orderId);
        map.put("count",count);
        map.put("fromFee",fromFee);
        map.put("toFee",toFee);
        map.put("fromFeeCoinCode",fromFeeCoinCode);
        map.put("toFeeCoinCode",toFeeCoinCode);
        return map;

    }

}
