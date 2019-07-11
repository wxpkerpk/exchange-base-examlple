package com.bitcola.exchange.message;


import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-02-12 12:42
 **/
@Data
public class MatchMessage {
    String type;
    String takerStatus;
    BigDecimal takerAmount;
    BigDecimal takerRemain;
    String orderId; // taker
    long timestamp;
    String pair;
    List<MatchRecordMessage> matchRecords = null;
    Map<String,OrderMessage> orderMap = new HashMap<>();
    public boolean hasMatchRecord() {
        return matchRecords != null && !matchRecords.isEmpty();
    }

    public List<MatchRecordMessage> getMatchRecords() {
        if (matchRecords == null){
            matchRecords = new ArrayList<>();
        }
        return matchRecords;
    }

    public void addMatchRecord(MatchRecordMessage matchRecord) {
        if (matchRecords == null) {
            matchRecords = new ArrayList<>();
        }
        matchRecords.add(matchRecord);
    }
}
