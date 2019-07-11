package com.bitcola.exchange.launchpad.rest;

import com.bitcola.exchange.launchpad.biz.ColaLaunchpadProjectBiz;
import com.bitcola.exchange.launchpad.config.DelayQueueBySpeed;
import com.bitcola.exchange.launchpad.config.DelayQueueBySpeedMap;
import com.bitcola.exchange.launchpad.dto.ColaLaunchpadProjectList;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadProjectIeo;
import com.bitcola.exchange.launchpad.feign.IExchangeFeign;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadProjectIeoMapper;
import com.bitcola.exchange.launchpad.message.BuyMessage;
import com.bitcola.exchange.launchpad.message.BuyResponse;
import com.bitcola.exchange.launchpad.service.BuyService;
import com.bitcola.exchange.launchpad.service.ClearService;
import com.bitcola.exchange.launchpad.message.ClearMessage;
import com.bitcola.exchange.launchpad.util.TCaptchaVerify;
import com.bitcola.exchange.launchpad.vo.BuyParams;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zkq
 * @create 2019-03-13 18:38
 **/
@RestController
@RequestMapping("project")
public class ColaLaunchpadProjectController {

    @Autowired
    ColaLaunchpadProjectBiz biz;

    @Autowired
    ColaLaunchpadProjectIeoMapper ieoMapper;

    @Autowired
    IExchangeFeign exchangeFeign;

    @Autowired
    DelayQueueBySpeedMap<ClearMessage> queueMap;

    @Autowired
    DelayQueueBySpeed<BuyMessage> buyQueue;

    @RequestMapping("list")
    public AppResponse list(Integer page){
        if (page == null || page == 0) page = 1;
        Map<String ,Object> map = new HashMap<>();
        List<ColaLaunchpadProjectList> list = biz.list(page);
        long totalPage = biz.total();
        map.put("page",page);
        map.put("total",totalPage);
        map.put("list",list);
        return AppResponse.ok().data(map);
    }

    @RequestMapping("detail")
    public AppResponse detail(String id){
        if (StringUtils.isBlank(id)) {
            return AppResponse.paramsError();
        }
        return AppResponse.ok().data(biz.detail(id));
    }

    @RequestMapping(value = "buy",method = RequestMethod.POST)
    public AppResponse buy(@RequestBody BuyParams params,HttpServletRequest request){
        if (StringUtils.isAnyBlank(params.getId(),params.getPin(),params.getRand(),params.getSymbol(),params.getTicket())){
            return AppResponse.paramsError();
        }
        if (params.getNumber() == null || params.getNumber().compareTo(BigDecimal.ZERO)<=0){
            return AppResponse.paramsError();
        }
        params.setNumber(params.getNumber().setScale(0,RoundingMode.DOWN));
        //  验证腾讯防水墙
        int i = TCaptchaVerify.verifyTicket(params.getTicket(), params.getRand(), getIp(request));
        if (i == -1) {
            System.out.println("防水墙错误");
            return AppResponse.error(ColaLanguage.get(ColaLanguage.VERIFY_FAILED));
        } else if (i <= 90){
            System.out.println("防水墙验证成功");
        } else {
            System.out.println("防水墙验证拦截 恶意等级:"+i);
            return AppResponse.error(ColaLanguage.get(ColaLanguage.VERIFY_FAILED));
        }
        String userID = BaseContextHandler.getUserID();
        String id = UUID.randomUUID().toString();
        Object lock = new Object();
        synchronized (lock){
            buyQueue.putMessage(new BuyMessage(id,lock,userID,params));
            try {
                lock.wait(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        BuyResponse buyResponse = BuyService.RESPONSE.get(id);
        if (buyResponse == null) {
            return AppResponse.error(ColaLanguage.get(ColaLanguage.LAUNCHPAD_BUY_BUSY));
        }
        if (buyResponse.isSuccess()){
            return AppResponse.ok().data(buyResponse.getNumber());
        } else {
            if (buyResponse.getErrorCode() == ResponseCode.TIP_ERROR_CODE){
                return AppResponse.error(ColaLanguage.get(buyResponse.getErrorMsg()));
            } else {
                return AppResponse.error(buyResponse.getErrorCode(),buyResponse.getErrorMsg());
            }
        }

    }

    private String getIp(HttpServletRequest req){
        String ip = req.getHeader("x-user-ip");
        if (StringUtils.isBlank(ip)){
            ip = req.getHeader("x-real-ip");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("x-forwarded-for");
            if (ip != null && ip.split(",").length>1){
                ip = ip.split(",")[0];
            }
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }

}
