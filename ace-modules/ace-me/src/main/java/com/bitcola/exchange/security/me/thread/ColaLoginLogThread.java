package com.bitcola.exchange.security.me.thread;

import com.bitcola.exchange.security.me.config.SpringBeanHandler;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.mapper.ColaLoginLogMapper;
import com.bitcola.me.entity.ColaLoginLog;

/**
 * 登录日志线程
 *      这里可以判断登录是否是异地登录,然后发送邮件
 *
 * @author zkq
 * @create 2018-09-30 16:23
 **/
public class ColaLoginLogThread implements Runnable {


    private ColaLoginLog colaLoginLog;

    private ColaLoginLogMapper loginLogMapper;

    public ColaLoginLogThread(ColaLoginLog colaLoginLog){
        this.colaLoginLog = colaLoginLog;
    }


    @Override
    public void run() {
        try {
            if (loginLogMapper == null){
                loginLogMapper = SpringBeanHandler.getBean("colaLoginLogMapper",ColaLoginLogMapper.class);
            }
            IPushFeign pushFeign = SpringBeanHandler.getBean(IPushFeign.class);
            // 通过 ip 获得区域
            colaLoginLog.setArea(pushFeign.getAddress(colaLoginLog.getIp()));
            loginLogMapper.insert(colaLoginLog);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
