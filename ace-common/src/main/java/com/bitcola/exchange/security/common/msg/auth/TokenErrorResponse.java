package com.bitcola.exchange.security.common.msg.auth;

import com.bitcola.exchange.security.common.constant.RestCodeConstants;
import com.bitcola.exchange.security.common.msg.BaseResponse;

/**
 * Created by wx on 2017/8/23.
 */
public class TokenErrorResponse extends BaseResponse {
    public TokenErrorResponse(String message) {
        super(RestCodeConstants.TOKEN_ERROR_CODE, message);
    }
}
