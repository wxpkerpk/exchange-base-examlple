package com.bitcola.exchange.security.common.exception.auth;


import com.bitcola.exchange.security.common.constant.CommonConstants;
import com.bitcola.exchange.security.common.exception.BaseException;

/**
 * Created by wx on 2017/9/10.
 */
public class ClientInvalidException extends BaseException {
    public ClientInvalidException(String message) {
        super(message, CommonConstants.EX_CLIENT_INVALID_CODE);
    }
}
