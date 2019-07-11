package com.bitcola.exchange.launchpad.rest;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.launchpad.biz.ColaLaunchpadApplyBiz;
import com.bitcola.exchange.launchpad.constant.LauchpadStatus;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadApply;
import com.bitcola.exchange.launchpad.util.ParamsUtil;
import com.bitcola.exchange.launchpad.vo.ColaLaunchpadApplyVo;
import com.bitcola.exchange.launchpad.vo.IeoParams;
import com.bitcola.exchange.launchpad.vo.ProjectParams;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.BaseResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.exchange.security.common.util.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author zkq
 * @create 2019-03-13 15:35
 **/
@RestController
@RequestMapping("apply")
public class ColaLaunchpadApplyController {


    @Autowired
    ColaLaunchpadApplyBiz biz;


    @RequestMapping("status")
    public AppResponse status(){
        String userID = BaseContextHandler.getUserID();
        if (StringUtils.isBlank(userID)){
            return AppResponse.error(ResponseCode.TOKEN_ERROR_CODE,ResponseCode.TOKEN_ERROR_MESSAGE);
        }
        Map<String,String> map = new HashMap<>();
        ColaLaunchpadApply apply = biz.selectById(userID);
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
        ColaLaunchpadApply apply = biz.selectById(userID);
        boolean exist = true;
        if (apply==null){
            exist = false;
            apply = new ColaLaunchpadApply();
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
        ColaLaunchpadApply apply = biz.selectById(userId);
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
        TableResultResponse<ColaLaunchpadApplyVo> result = biz.page(query);
        return result;
    }


    @RequestMapping(value = "publishProject",method = RequestMethod.POST)
    public AppResponse publishProject(@RequestBody ProjectParams params){
        String userID = BaseContextHandler.getUserID();
        if (!UserConstant.SYS_ADMIN.equals(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        if (StringUtils.isAnyBlank(params.getCoinCode(),params.getUserId())){
            return AppResponse.paramsError();
        }
        if (params.getDetail().size() == 0 || params.getDetailCn().size() == 0){
            return AppResponse.paramsError();
        }
        params.setId(UUID.randomUUID().toString());
        params.setTimestamp(System.currentTimeMillis());
        params.setCommunityStr(JSONObject.toJSONString(params.getCommunity()));
        params.setDetailStr(ParamsUtil.listToString(params.getDetail(),ParamsUtil.COMMA_5_SPLIT));
        params.setDetailCnStr(ParamsUtil.listToString(params.getDetailCn(),ParamsUtil.COMMA_5_SPLIT));
        biz.saveProject(params);
        return AppResponse.ok();
    }

    @RequestMapping(value = "publishIeo",method = RequestMethod.POST)
    public AppResponse publishIeo(@RequestBody IeoParams params){
        String userID = BaseContextHandler.getUserID();
        if (!UserConstant.SYS_ADMIN.equals(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        if (StringUtils.isAnyBlank(params.getProjectId(),params.getUserId(),params.getCoinCode(),params.getTitle(),params.getTitleImg(),
                params.getIntroduction(),params.getIntroductionCn(),params.getTitleCn())){
            return AppResponse.paramsError();
        }
        if (isAnyNull(params.getStart(), params.getEnd(), params.getPrice(), params.getNumber(),params.getAllowMinNumber(),params.getAllowMaxNumber(),
                params.getAllowTotalNumber())) {
            return AppResponse.paramsError();
        }
        if (params.getStart() == 0 || params.getEnd() == 0 || params.getIssueTime() == 0 ||
                params.getPrice().compareTo(BigDecimal.ZERO) == 0 || params.getNumber().compareTo(BigDecimal.ZERO) == 0 ||
                params.getAllowMinNumber().compareTo(BigDecimal.ZERO) == 0 || params.getAllowMaxNumber().compareTo(BigDecimal.ZERO) == 0 ||
                params.getAllowTotalNumber().compareTo(BigDecimal.ZERO) == 0){
            return AppResponse.paramsError();
        }
        params.setRemain(params.getNumber());
        params.setId(UUID.randomUUID().toString());
        params.setSymbolStr(ParamsUtil.listToString(params.getSymbols(),ParamsUtil.COMMA_SPLIT));
        biz.startIeo(params);
        return AppResponse.ok();
    }

    private boolean isAnyNull(Object... obj){
        for (Object o : obj) {
            if (o == null) return true;
        }
        return false;
    }

    @RequestMapping(value = "projectList",method = RequestMethod.GET)
    public BaseResponse projectList(@RequestParam Map<String, Object> params){
        String userID = BaseContextHandler.getUserID();
        if (!UserConstant.SYS_ADMIN.equals(userID)){
            return new BaseResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        AdminQuery query = new AdminQuery(params);
        return biz.projectList(query);
    }

    @RequestMapping(value = "ieoList",method = RequestMethod.GET)
    public AppResponse ieoList(String id){
        String userID = BaseContextHandler.getUserID();
        if (!UserConstant.SYS_ADMIN.equals(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        if (StringUtils.isBlank(id)){
            return AppResponse.paramsError();
        }
        List<IeoParams> list = biz.ieoList(id);
        return AppResponse.ok().data(list);
    }



}
