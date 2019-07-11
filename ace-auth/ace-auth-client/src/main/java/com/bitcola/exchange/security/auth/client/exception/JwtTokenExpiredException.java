package com.bitcola.exchange.security.auth.client.exception;

/**
 * Created by wx on 2017/9/15.
 */
public class JwtTokenExpiredException extends Exception {
    public JwtTokenExpiredException(String s) {
        super(s);
    }
}
