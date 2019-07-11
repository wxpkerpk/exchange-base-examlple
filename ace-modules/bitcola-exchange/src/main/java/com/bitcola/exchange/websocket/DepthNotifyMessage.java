package com.bitcola.exchange.websocket;

import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author zkq
 * @create 2019-02-15 14:59
 **/
@Data
public class DepthNotifyMessage {
    Map<BigDecimal,BigDecimal> ask = new TreeMap<>(new Comparator<BigDecimal>() {
        @Override
        public int compare(BigDecimal o1, BigDecimal o2) {
            return o2.compareTo(o1);
        }
    });
    Map<BigDecimal,BigDecimal> bids = new TreeMap<>(new Comparator<BigDecimal>() {
        @Override
        public int compare(BigDecimal o1, BigDecimal o2) {
            return o1.compareTo(o2);
        }
    });
    public Object getResult(){
        Map<String,Object> map = new HashMap<>();
        List<BigDecimal[]> askList = new ArrayList<>();
        for (BigDecimal key : ask.keySet()) {
            askList.add(new BigDecimal[]{key,ask.get(key)});
        }
        List<BigDecimal[]> bidsList = new ArrayList<>();
        for (BigDecimal key : bids.keySet()) {
            bidsList.add(new BigDecimal[]{key,bids.get(key)});
        }
        map.put("ask",askList);
        map.put("bids",bidsList);
        return map;
    }
}
