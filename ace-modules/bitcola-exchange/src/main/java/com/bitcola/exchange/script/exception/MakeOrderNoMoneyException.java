package com.bitcola.exchange.script.exception;

/**
 * @author zkq
 * @create 2019-03-21 11:02
 **/
public class MakeOrderNoMoneyException extends MakeOrderException {
    public MakeOrderNoMoneyException() {
        super(1001, "no money");
    }
}
