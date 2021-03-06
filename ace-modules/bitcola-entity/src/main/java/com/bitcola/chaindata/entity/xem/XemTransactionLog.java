package com.bitcola.chaindata.entity.xem;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-11-23 18:28
 **/
@Data
public class XemTransactionLog {
    String txId; // 展示用,无法用于查询
    String hashId; // hash 的 id, 用于分页查询
    BigDecimal number;
    BigDecimal fees;
    String to;
    String memo;
    String coin; // xem 或者 代币
    boolean confirm;
}
