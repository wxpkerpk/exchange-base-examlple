package com.bitcola.exchange.launchpad.rest;

import com.bitcola.exchange.launchpad.biz.ColaLaunchpadExchangeLogBiz;
import com.bitcola.exchange.launchpad.biz.ColaLaunchpadProjectBiz;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadExchangeLog;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.BaseResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-03-15 12:33
 **/
@RestController
@RequestMapping("exchangeLog")
public class ColaLaunchpadExchangeLogController {

    @Autowired
    ColaLaunchpadExchangeLogBiz biz;

    @RequestMapping("order")
    public TableResultResponse order(@RequestParam Map<String,Object> params){
        AdminQuery query = new AdminQuery(params);
        query.put("userId", BaseContextHandler.getUserID());
        List<ColaLaunchpadExchangeLog> list = biz.list(query);
        Long total = biz.total(query);
        return new TableResultResponse(total,list);
    }

    @RequestMapping("log")
    public BaseResponse log(@RequestParam Map<String,Object> params){
        String userID = BaseContextHandler.getUserID();
        if (!UserConstant.SYS_ADMIN.equals(userID)){
            return new BaseResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        AdminQuery query = new AdminQuery(params);
        List<ColaLaunchpadExchangeLog> list = biz.list(query);
        Long total = biz.total(query);
        return new TableResultResponse(total,list);
    }

    @RequestMapping("issue")
    public AppResponse issue(String projectId, Boolean containProject, BigDecimal projectRate){
        if (StringUtils.isBlank(projectId)){
            return AppResponse.paramsError();
        }
        String userID = BaseContextHandler.getUserID();
        if (!UserConstant.SYS_ADMIN.equals(userID)){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        Map<String,Object> result = biz.issue(projectId,containProject,projectRate);
        return AppResponse.ok().data(result);
    }



}
