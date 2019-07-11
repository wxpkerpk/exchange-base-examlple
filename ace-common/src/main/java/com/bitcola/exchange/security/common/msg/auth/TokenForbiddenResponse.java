package com.bitcola.exchange.security.common.msg.auth;

import com.bitcola.exchange.security.common.constant.RestCodeConstants;
import com.bitcola.exchange.security.common.msg.BaseResponse;

/**
 * Created by wx on 2017/8/25.
 */
public class TokenForbiddenResponse  extends BaseResponse {
    public TokenForbiddenResponse(String message) {
        super(RestCodeConstants.TOKEN_FORBIDDEN_CODE, message);
    }
}
