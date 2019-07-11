package com.bitcola.chain.chain.tera.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-03-08 12:33
 **/
@Data
public class Account {
    String privateKey;
    String publicKey;
    long accountID;
    String name;
    int result;
}
