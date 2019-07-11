package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.CSVUtil;
import com.bitcola.exchange.security.common.util.Query;
import com.bitcola.exchange.security.me.biz.ColaLoginLogBiz;
import com.bitcola.exchange.security.me.biz.ColaUserBiz;
import com.bitcola.me.entity.ColaUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-10-11 12:31
 **/
@RestController
@RequestMapping("login")
public class ColaLoginLogController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private ColaUserBiz userBiz;

    @Autowired
    ColaLoginLogBiz biz;

    @RequestMapping("log")
    public TableResultResponse log(@RequestParam Map<String,Object> params){
        Query query = new Query(params);
        if (query.getLimit() > 100){
            return null;
        }
        TableResultResponse result = biz.log(query);
        return result;
    }

    @RequestMapping("csv")
    public void csv(HttpServletResponse response) throws IOException {
        String[] headers = {"Nº","Times","IP address","Login method","status"};
        if (ColaLanguage.getCurrentLanguage().equals(ColaLanguage.LANGUAGE_CN)){
            headers = new String[]{"Nº","时间","IP 地址","登录方式","登录状态"};
        }
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment;filename=log.csv");
        List<Object[]> list = biz.csv();
        CSVUtil.downloadCVS(response.getOutputStream(),headers,list);
    }

    @RequestMapping("logOut")
    public AppResponse logOut(){
        ColaUserEntity user = userBiz.info(BaseContextHandler.getUserID());
        String o = redisTemplate.opsForValue().get(UserConstant.USER_LOGIN_KEY+user.getSysUserID());
        if (o!=null){
            redisTemplate.delete(UserConstant.USER_LOGIN_KEY+user.getSysUserID());
            redisTemplate.delete(UserConstant.USER_LOGIN_TOKEN_KEY+user.getSysUserID()+o);
        }
        return AppResponse.ok();
    }


}
