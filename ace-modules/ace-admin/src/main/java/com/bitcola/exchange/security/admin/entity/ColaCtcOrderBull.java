package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 
 * 进出账单
 * @author zkq
 * @create 2019-05-08 11:21
 **/
@Data
@Table(name = "ag_admin_v1.cola_ctc_order_bull")
public class ColaCtcOrderBull {

    String id;
    @Column(name = "order_id")
    String orderId;
    @Column(name = "from_card_id")
    String fromCardId;
    @Column(name = "to_card_id")
    String toCardId;
    Long timestamp;
    /**
     * 金额
     */
    BigDecimal amount;
    /**
     * 银行流水号
     */
    @Column(name = "bank_serial_number")
    String bankSerialNumber;
    /**
     * 从(姓名)
     */
    @Column(name = "from_customer")
    String fromCustomer;
    /**
     * 到(姓名)
     */
    @Column(name = "to_customer")
    String toCustomer;
    /**
     * 对于公司是进账还是出账,in,out
     */
    String type;
}
