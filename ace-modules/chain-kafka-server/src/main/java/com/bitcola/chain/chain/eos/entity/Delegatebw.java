package com.bitcola.chain.chain.eos.entity;

import lombok.Data;

import java.util.Map;

/**
 *
 * 租借
 * @author zkq
 * @create 2018-12-12 21:02
 **/
@Data
public class Delegatebw {
    String code;
    String action;
    Map<String,Object> args;
}
