package com.bitcola.exchange.websocket;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2019-02-20 12:17
 **/
@Data
public class OrderNotifyMessage {
    List<OrderNotifyEntity> list = new ArrayList<>();
}
