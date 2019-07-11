package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaChatBiz;
import com.bitcola.exchange.security.me.feign.IDataServiceFeign;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-13 11:22
 **/
@RestController
@RequestMapping("chat")
public class ColaChatController {

    @Autowired
    ColaChatBiz biz;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    /**
     * 检查是是否重复,是否为非法词汇
     * @param groupName
     * @return
     */
    @RequestMapping("checkGroupName")
    public AppResponse checkGroupName(String groupName){
        if (StringUtils.isBlank(groupName)) return AppResponse.paramsError();
        if (dataServiceFeign.contain(groupName) || dataServiceFeign.contain(groupName) || dataServiceFeign.contain(groupName)){
            return AppResponse.ok().data("FORBIDDEN");
        } else if (!isOk(groupName)){
            return AppResponse.ok().data("FORBIDDEN");
        }
        boolean repeat = biz.checkGroupNameRepeat(groupName);
        if (repeat) {
            return AppResponse.ok().data("REPEAT");
        }
        return AppResponse.ok().data("OK");
    }


    @RequestMapping(value = "saveOrUpdateGroup",method = RequestMethod.POST)
    public AppResponse saveOrUpdateGroup(@RequestBody Map<String,String> params){
        String id = params.get("id");
        String avatar = params.get("avatar");
        String name = params.get("groupName");
        if (StringUtils.isAnyBlank(id)){
            return AppResponse.paramsError();
        }
        if (StringUtils.isBlank(name)) return AppResponse.paramsError();
        if (dataServiceFeign.contain(name) || dataServiceFeign.contain(name) || dataServiceFeign.contain(name)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_INPUT_LIMIT));
        }
        if (!isOk(name)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_INPUT_LIMIT));
        }
        biz.saveOrUpdateGroup(id,avatar,name);
        return AppResponse.ok();
    }


    @RequestMapping(value = "groupInfo",method = RequestMethod.POST)
    public AppResponse groupInfo(@RequestBody List<String> ids){

        List<Map<String,String>> list = biz.groupInfo(ids);
        return AppResponse.ok().data(list);
    }

    private boolean isOk(String... str){
        for (String s : str) {
            s = s.toLowerCase();
            if (s.contains("cola")) return false;
            if (s.contains("币可")) return false;
            if (s.contains("可乐")) return false;
        }
        return true;
    }

}
