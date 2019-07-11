package com.bitcola.exchange.launchpad.rest;

import com.bitcola.exchange.launchpad.biz.ColaLaunchpadWhitelistBiz;
import com.bitcola.exchange.launchpad.constant.LauchpadStatus;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadWhitelist;
import com.bitcola.exchange.launchpad.util.ParamsUtil;
import com.bitcola.exchange.launchpad.vo.ColaLaunchpadWhitelistVo;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.BaseResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-03-13 15:35
 **/
@RestController
@RequestMapping("whitelist")
public class ColaLaunchpadWhitelistController {


    @Autowired
    ColaLaunchpadWhitelistBiz biz;


    @RequestMapping("status")
    public AppResponse status(){
        String userID = BaseContextHandler.getUserID();
        if (StringUtils.isBlank(userID)){
            return AppResponse.error(ResponseCode.TOKEN_ERROR_CODE,ResponseCode.TOKEN_ERROR_MESSAGE);
        }
        Map<String,String> map = new HashMap<>();
        ColaLaunchpadWhitelist apply = biz.selectById(userID);
        String status;
        String reason = null;
        if (apply == null){
            status = LauchpadStatus.NOT_RECORD;
        } else {
            status = apply.getStatus();
            reason = apply.getReason();
        }
        map.put("status",status);
        map.put("reason",reason);
        return AppResponse.ok().data(map);
    }


    @RequestMapping(value = "apply",method = RequestMethod.POST)
    public AppResponse apply(@RequestBody Map<String,String> params){
        String userID = BaseContextHandler.getUserID();
        if (StringUtils.isBlank(userID)){
            return AppResponse.error(ResponseCode.TOKEN_ERROR_CODE,ResponseCode.TOKEN_ERROR_MESSAGE);
        }
        ColaLaunchpadWhitelist apply = biz.selectById(userID);
        boolean exist = true;
        if (apply==null){
            exist = false;
            apply = new ColaLaunchpadWhitelist();
        }
        if (LauchpadStatus.PENDING.equals(apply.getStatus()) || LauchpadStatus.SUCCESS.equals(apply.getStatus())){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        apply.setReason(null);
        apply.setUserId(BaseContextHandler.getUserID());
        apply.setStatus(LauchpadStatus.PENDING);
        apply.setDetail(ParamsUtil.mapToString(params));
        if (exist){
            biz.updateById(apply);
        } else {
            biz.insert(apply);
        }
        return AppResponse.ok();
    }

    @RequestMapping("audit")
    public AppResponse audit(String userId,String status,String reason){
        String userID = BaseContextHandler.getUserID();
        if (!UserConstant.SYS_ADMIN.equals(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        ColaLaunchpadWhitelist apply = biz.selectById(userId);
        apply.setStatus(status);
        apply.setReason(reason);
        biz.updateSelectiveById(apply);
        return AppResponse.ok();
    }


    @RequestMapping("page")
    public BaseResponse page(@RequestParam Map<String, Object> params){
        String userID = BaseContextHandler.getUserID();
        if (!UserConstant.SYS_ADMIN.equals(userID)){
            return new BaseResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        Query query = new Query(params);
        TableResultResponse<ColaLaunchpadWhitelistVo> result = biz.page(query);
        return result;
    }


}
