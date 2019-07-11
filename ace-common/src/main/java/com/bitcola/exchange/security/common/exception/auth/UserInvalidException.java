package com.bitcola.exchange.security.common.exception.auth;


import com.bitcola.exchange.security.common.constant.CommonConstants;
import com.bitcola.exchange.security.common.exception.BaseException;

/**
 * Created by wx on 2017/9/8.
 */
public class UserInvalidException extends BaseException {
    public UserInvalidException(String message) {
        super(message, CommonConstants.EX_USER_PASS_INVALID_CODE);
    }
}
