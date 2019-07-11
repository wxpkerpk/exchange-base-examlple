package com.bitcola.exchange.security.common.exception.auth;


import com.bitcola.exchange.security.common.constant.CommonConstants;
import com.bitcola.exchange.security.common.exception.BaseException;

/**
 * Created by wx on 2017/9/12.
 */
public class ClientForbiddenException extends BaseException {
    public ClientForbiddenException(String message) {
        super(message, CommonConstants.EX_CLIENT_FORBIDDEN_CODE);
    }

}
