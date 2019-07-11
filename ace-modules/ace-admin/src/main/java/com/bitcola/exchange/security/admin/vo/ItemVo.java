package com.bitcola.exchange.security.admin.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2018-11-01 10:56
 **/
@Data
public class ItemVo {
    String id;
    String key;
    List<ItemVo> sub;

    public List<ItemVo> getSub() {
        if (sub == null)
            sub = new ArrayList<>();
        return sub;
    }

}
