package com.bitcola.chaindata.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-12-28 21:43
 **/
@Data
public class WithdrawResponse {
    boolean checked;
    String data;
    String reason;
}
