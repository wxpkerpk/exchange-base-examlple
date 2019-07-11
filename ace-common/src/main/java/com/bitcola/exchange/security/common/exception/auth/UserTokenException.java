package com.bitcola.exchange.security.common.exception.auth;


import com.bitcola.exchange.security.common.constant.CommonConstants;
import com.bitcola.exchange.security.common.exception.BaseException;

/**
 * Created by wx on 2017/9/8.
 */
public class UserTokenException extends BaseException {
    public UserTokenException(String message) {
        super(message, CommonConstants.EX_USER_INVALID_CODE);
    }
}
