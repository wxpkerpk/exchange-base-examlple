package com.bitcola.chain.chain.eos.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-12-13 12:10
 **/
@Data
public class Action {
    String account;
    String name;
    String data;
    List<Map<String,String>> authorization;
}
