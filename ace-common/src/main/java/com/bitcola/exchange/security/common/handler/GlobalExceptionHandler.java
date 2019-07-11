package com.bitcola.exchange.security.common.handler;

import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.BaseResponse;
import com.bitcola.exchange.security.common.constant.CommonConstants;
import com.bitcola.exchange.security.common.exception.BaseException;
import com.bitcola.exchange.security.common.exception.auth.ClientTokenException;
import com.bitcola.exchange.security.common.exception.auth.UserInvalidException;
import com.bitcola.exchange.security.common.exception.auth.UserTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by wx on 2017/9/8.
 */
@ControllerAdvice("com.bitcola")
@ResponseBody
public class GlobalExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(ExpiredJwtException.class)
    public AppResponse userTokenExpiredJwtExceptionHandler(HttpServletResponse response, ExpiredJwtException ex) {
        logger.error(ex.getMessage(),ex);
        return new AppResponse(ResponseCode.TOKEN_ERROR_CODE, ex.getMessage());
    }

    @ExceptionHandler(ClientTokenException.class)
    public AppResponse clientTokenExceptionHandler(HttpServletResponse response, ClientTokenException ex) {
        response.setStatus(403);
        logger.error(ex.getMessage(),ex);
        return new AppResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(UserTokenException.class)
    public AppResponse userTokenExceptionHandler(HttpServletResponse response, UserTokenException ex) {
        response.setStatus(401);
        //logger.error(ex.getMessage(),ex);
        logger.error("token error");
        return new AppResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(UserInvalidException.class)
    public AppResponse userInvalidExceptionHandler(HttpServletResponse response, UserInvalidException ex) {
        response.setStatus(200);
        logger.error(ex.getMessage(),ex);
        return new AppResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public AppResponse baseExceptionHandler(HttpServletResponse response, BaseException ex) {
        logger.error(ex.getMessage(),ex);
        response.setStatus(500);
        return new AppResponse(ex.getStatus(), null);
    }

    @ExceptionHandler(Exception.class)
    public AppResponse otherExceptionHandler(HttpServletResponse response, Exception ex) {
        response.setStatus(500);
        logger.error(ex.getMessage(),ex);
        return new AppResponse(CommonConstants.EX_OTHER_CODE, null);
    }

}
