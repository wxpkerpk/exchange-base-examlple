package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-07 14:54
 **/
@Data
@Table(name = "ag_admin_v1.cola_ctc_business_card")
public class ColaCtcBusinessCard {
    @Id
    @Column(name = "card_id")
    String cardId;
    BigDecimal balance;
    Integer available;
}
