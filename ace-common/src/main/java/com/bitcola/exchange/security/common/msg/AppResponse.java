package com.bitcola.exchange.security.common.msg;

import com.bitcola.exchange.security.common.constant.ResponseCode;

/**
 * app 开发返回数据
 *
 * @see ResponseCode (返回状态码和信息)
 *
 * @author zkq
 * @create 2018-08-12 21:49
 **/
public class AppResponse<T>{

    public int status;

    public String message;

    public T data;

    public AppResponse(){
        this.status = ResponseCode.SUCCESS_CODE;
        this.message = ResponseCode.SUCCESS_MESSAGE;
    }

    public AppResponse(T data){
        this.status = ResponseCode.SUCCESS_CODE;
        this.message = ResponseCode.SUCCESS_MESSAGE;
        this.data = data;
    }

    public AppResponse(int status,String message){
        this.status = status;
        this.message = message;
    }

    public static AppResponse error(int status,String message){
        return new AppResponse(status,message);
    }

    /**
     * 用于返回错误提示信息
     * @param message
     * @return
     */
    public static AppResponse error(String message){
        return new AppResponse(ResponseCode.TIP_ERROR_CODE,message);
    }

    public static AppResponse paramsError(){
        return new AppResponse(ResponseCode.PARAMS_ERROR_CODE,ResponseCode.PARAMS_ERROR_MSG);
    }

    public static AppResponse ok(){
        return new AppResponse();
    }

    public AppResponse data(T data){
        this.setData(data);
        return this;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
