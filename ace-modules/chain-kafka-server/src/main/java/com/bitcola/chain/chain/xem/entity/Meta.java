package com.bitcola.chain.chain.xem.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-11-23 18:45
 **/
@Data
public class Meta {
    Hash hash;
    int height;
    int id;
    Hash innerHash;
}
