package com.bitcola.chaindata.entity.eth;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-09 15:54
 **/
@Data
public class ColaEthTransaction implements Serializable {
    String hash;
    String from;
    String to;
    boolean isToken = false;
    String contract;
    BigDecimal value;
    int confirm = 0;
}
