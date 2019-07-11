package com.bitcola.exchange.websocket;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zkq
 * @create 2019-02-15 17:02
 **/
@Data
public class PersonOrderNotifyMessage {
    Set<String> ids = new HashSet<>();
    String pair;

    public PersonOrderNotifyMessage(String pair, List<String> ids){
        this.pair = pair;
        this.ids.addAll(ids);
    }
}
