package com.bitcola.exchange.launchpad.message;

import com.bitcola.exchange.launchpad.vo.BuyParams;
import lombok.Data;

/**
 * @author zkq
 * @create 2019-03-15 15:49
 **/
@Data
public class BuyMessage {
    String id;
    Object lock;
    String userId;
    BuyParams params;
    public BuyMessage(String id,Object lock,String userId,BuyParams params){
        this.id = id;
        this.lock = lock;
        this.userId = userId;
        this.params = params;
    }
}
