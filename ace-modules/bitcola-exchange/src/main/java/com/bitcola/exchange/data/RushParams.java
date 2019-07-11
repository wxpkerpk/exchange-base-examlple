package com.bitcola.exchange.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2019-04-22 14:37
 **/
@Data
public class RushParams {
    String rushPair; // 抢购交易对
    String rushProjectUserId; // 抢购项目方 ID
    BigDecimal rushMaxLimit = BigDecimal.ZERO; // 抢购 USDT 限额
    List<BigDecimal> rushPrice = new ArrayList<>(); // 抢购价格,多轮以逗号分隔
    List<Long> rushTimestampStart = new ArrayList<>(); // 开始时间,以逗号分隔
    List<Long> rushTimestampEnd = new ArrayList<>(); // 结束时间,以逗号分隔
}
