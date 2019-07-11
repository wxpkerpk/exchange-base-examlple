package com.bitcola.dataservice.exception;

/*
 * @author:wx
 * @description:余额不足异常
 * @create:2018-08-06  19:07
 */
public class LACK_BALANCE_EXCEPTION extends Exception {
    public LACK_BALANCE_EXCEPTION(String 余额不足) {
        super(余额不足);
    }
}
