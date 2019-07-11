package com.bitcola.exchange.ctc.vo;

import lombok.Data;


/**
 * @author zkq
 * @create 2019-05-08 10:30
 **/
@Data
public class BankCardAddParams {
    String pin;
    String cardId;
    String bankAddress;
    String bankId;
    String ticket; // 腾讯防水墙验证
    String rand; // 腾讯防水墙验证
}
