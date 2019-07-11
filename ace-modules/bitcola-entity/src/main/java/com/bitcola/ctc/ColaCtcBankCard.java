package com.bitcola.ctc;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-05-07 14:47
 **/
@Data
@Table(name = "ag_admin_v1.cola_ctc_bank_card")
public class ColaCtcBankCard {

    @Id
    @Column(name = "card_id")
    String cardId;
    @Column(name = "user_id")
    String userId;
    @Column(name = "bank_address")
    String bankAddress;
    @Column(name = "bank_name")
    String bankName;
    @Column(name = "user_name")
    String userName;
    @JSONField(serialize = false)
    @Column(name = "document_number")
    String documentNumber;
    @JSONField(serialize = false)
    Integer checked = 0; //是否通过第三方校验
    @JSONField(serialize = false)
    @Column(name = "white_icon")
    String whiteIcon;
    String icon;

    @JSONField(serialize = false)
    @JsonIgnore
    String sign;
}
