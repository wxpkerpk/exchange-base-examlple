package com.bitcola.exchange.script.exception;

/**
 * @author zkq
 * @create 2019-03-21 10:58
 **/
public class MakeOrderException extends RuntimeException {

    int errorCode = 200;
    public MakeOrderException(int errorCode,String errorMessage){
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
