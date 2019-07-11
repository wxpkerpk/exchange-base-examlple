package com.bitcola.exchange.security.me.biz;


import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.Query;
import com.bitcola.exchange.security.common.util.TimeUtils;
import com.bitcola.exchange.security.me.mapper.ColaLoginLogMapper;
import com.bitcola.exchange.security.me.vo.LoginLogVo;
import com.bitcola.me.entity.ColaLoginLog;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录日志表
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-30 16:19:50
 */
@Service
public class ColaLoginLogBiz extends BaseBiz<ColaLoginLogMapper, ColaLoginLog> {
    public TableResultResponse log(Query query) {
        query.put("userId",BaseContextHandler.getUserID());
        List<LoginLogVo> list = new ArrayList<>();
        List<ColaLoginLog> logs = mapper.log(BaseContextHandler.getUserID(),query.getPage(),query.getLimit());
        for (int i = 0;i<logs.size();i++) {
            LoginLogVo vo = new LoginLogVo();
            ColaLoginLog colaLoginLog = logs.get(i);
            BeanUtils.copyProperties(colaLoginLog,vo);
            if (ColaLanguage.getCurrentLanguage().equals(ColaLanguage.LANGUAGE_CN)){
                vo.setAddress(colaLoginLog.getArea());
            }
            vo.setIndex(i+1);
            if (!"UnKnown".equals(colaLoginLog.getPlatform()) && !"UnKnown".equals(colaLoginLog.getDevice())){
                if ("App".equals(colaLoginLog.getPlatform())){
                    vo.setLoginMethod(colaLoginLog.getDevice() + " App");
                } else {
                    vo.setLoginMethod(colaLoginLog.getDevice()+" on "+colaLoginLog.getPlatform());
                }
            } else {
                vo.setLoginMethod("UnKnown");
            }
            list.add(vo);
        }
        Integer result = mapper.countLog(BaseContextHandler.getUserID());
        return new TableResultResponse(result,list);
    }


    public List<Object[]> csv() {
        List<Object[]> list = new ArrayList<>();
        List<ColaLoginLog> logs = mapper.csv(BaseContextHandler.getUserID());
        for (int i = 0;i<logs.size();i++) {
            ColaLoginLog log = logs.get(i);
            String method = "";
            if (!"UnKnown".equals(log.getPlatform()) && !"UnKnown".equals(log.getDevice())){
                if ("App".equals(log.getPlatform())){
                    method = log.getDevice() + " App";
                } else {
                    method = log.getDevice()+" on "+log.getPlatform();
                }
            } else {
                method = "UnKnown";
            }
            Object[] o = {i+1, TimeUtils.getDateTimeFormat(log.getTime()),log.getIp(),method,log.getStatus()};
            list.add(o);
        }
        return list;
    }
}