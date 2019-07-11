package com.bitcola.exchange.security.auth.client.exception;

/**
 *
 * @author wx
 * @date 2017/9/15
 */
public class JwtSignatureException extends Exception {
    public JwtSignatureException(String s) {
        super(s);
    }
}
