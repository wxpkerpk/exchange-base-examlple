package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaUserBiz;
import com.bitcola.exchange.security.admin.biz.ColaUserLimitBiz;
import com.bitcola.exchange.security.admin.entity.SysUserEntity;
import com.bitcola.exchange.security.admin.feign.ICommunityFeign;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.exchange.security.common.util.Query;
import com.bitcola.me.entity.ColaUserKyc;
import com.bitcola.me.entity.ColaUserLimit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户管理
 *
 * @author zkq
 * @create 2018-10-30 19:01
 **/
@RestController
@RequestMapping("cola/user")
public class ColaUserController {

    @Autowired
    ColaUserBiz biz;

    @Autowired
    ColaUserLimitBiz userLimitBiz;
    @Autowired
    ICommunityFeign communityFeign;

    @RequestMapping("list")
    public TableResultResponse<SysUserEntity> list(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return biz.selectByQuery(query);
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public Map<String,Object> one(@PathVariable String id){
        return biz.one(id);
    }

    @RequestMapping("kycList")
    public TableResultResponse<ColaUserKyc> kycList(@RequestParam Map<String, Object> params){
        Query query = new Query(params);
        return biz.kycList(query);
    }
    @RequestMapping(value = "auditKyc",method = RequestMethod.POST)
    public AppResponse auditKyc(@RequestBody Map<String, Object> params){
        String userId = params.get("userId").toString();
        Integer status = (Integer)params.get("status");
        if (status == null){
            return AppResponse.paramsError();
        }
        if (!(-2 == status||1==status)){
            return AppResponse.paramsError();
        }
        String reason = params.get("reason").toString();
        biz.auditKyc(userId,status,reason);
        return AppResponse.ok();
    }

    @RequestMapping("kyc/{userId}")
    public AppResponse kycDetail(@PathVariable String userId){
        Map<String,Object> map = biz.kycDetail(userId);
        return AppResponse.ok().data(map);
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    public AppResponse update(@RequestBody Map<String,Object> params){
        String id = params.get("id").toString();
        if (StringUtils.isBlank(id)){
            return AppResponse.paramsError();
        }
        String username = (String)params.get("username");
        String pin = (String)params.get("pin");
        String email = (String)params.get("email");
        String telephone = (String)params.get("telephone");
        String areaCode = (String)params.get("areaCode");
        Integer enable = (Integer)params.get("enable");
        if (!StringUtils.isAnyBlank(username,email,areaCode,telephone)){
            Integer repeat = biz.repeat(username,email,areaCode,telephone);
            if (repeat>=1){
                return AppResponse.error("输入的内容重复了");
            }
        }
        biz.update(id,username,pin,enable,email,telephone,areaCode);
        return AppResponse.ok();
    }


    /**
     *
     * @return
     */
    @RequestMapping("userLimit")
    public AppResponse userLimit(String userId,Long limitTime,String reason,String module,String id,String type){
        if (!adminIds.contains(BaseContextHandler.getUserID()) && Integer.valueOf(BaseContextHandler.getUserID())>100000){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        Integer limitType = 0;
        if (limitTime == null || limitTime == 0){
            limitType = 1;
        } else {
            limitTime = System.currentTimeMillis()+limitTime;
        }
        ColaUserLimit userLimit = userLimitBiz.getUserLimit(userId, module);
        if (userLimit == null){
            userLimit = new ColaUserLimit();
            userLimit.setId(UUID.randomUUID().toString());
            userLimit.setUserId(userId);
            userLimit.setModule(module);
            userLimit.setType(limitType);
            userLimit.setTime(limitTime);
            userLimit.setReason(reason);
            userLimitBiz.insert(userLimit);
        } else {
            userLimit.setType(limitType);
            userLimit.setTime(limitTime);
            userLimit.setReason(reason);
            userLimitBiz.updateById(userLimit);
        }
        if ("community".equals(module)){
            if(StringUtils.isNotBlank(id)&&StringUtils.isNotBlank(type)){
                communityFeign.removeItem(id,type);
            }
        }
        return AppResponse.ok();
    }

    @RequestMapping("unLimit")
    public AppResponse unLimit(String id){
        userLimitBiz.deleteById(id);
        return AppResponse.ok();
    }


    @RequestMapping("limitPage")
    public TableResultResponse<ColaUserLimit> limitPage(@RequestParam Map<String, Object> params){
        Query query = new Query(params);
        return userLimitBiz.selectByQuery(query);
    }



    private static List<String> adminIds = new ArrayList<>();
    static {
        adminIds.add("200011");
        adminIds.add("200147");
        adminIds.add("207492");
    }

    @RequestMapping("inviter/inviterList")
    public TableResultResponse inviterList(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        String userId = params.get("userId").toString();
        if (StringUtils.isBlank(userId)){
            return new TableResultResponse();
        }
        return biz.inviterList(query);
    }


    @RequestMapping("userAddressList")
    public TableResultResponse userAddressList(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return biz.userAddressList(query);
    }


}
