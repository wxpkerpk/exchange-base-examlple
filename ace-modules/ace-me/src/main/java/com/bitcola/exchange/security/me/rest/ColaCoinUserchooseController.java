package com.bitcola.exchange.security.me.rest;


import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.exchange.security.me.biz.ColaCoinUserchooseBiz;
import com.bitcola.me.entity.ColaCoinUserchoose;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("colaCoinUserchoose")
public class ColaCoinUserchooseController extends BaseController<ColaCoinUserchooseBiz, ColaCoinUserchoose> {

    /**
     * 添加自选
     * @return
     */
    @RequestMapping(value = "insert",method = RequestMethod.POST)
    public AppResponse insert(@RequestBody ColaCoinUserchoose userchoose){
        if (StringUtils.isNotBlank(userchoose.getPair())){
            String[] s = userchoose.getPair().split("_");
            userchoose.setCoinCode(s[0]);
            userchoose.setSymbol(s[1]);
        }
        if (StringUtils.isAnyBlank(userchoose.getCoinCode(),userchoose.getSymbol())){
            return AppResponse.paramsError();
        }
        int i = baseBiz.isExist(userchoose.getCoinCode(),userchoose.getSymbol());
        if (i == 1){
            return AppResponse.ok().data(false);
        }
        userchoose.setId(UUID.randomUUID().toString());
        userchoose.setUserId(BaseContextHandler.getUserID());
        baseBiz.insertSelective(userchoose);
        return AppResponse.ok();
    }

    /**
     * 移除自选
     * @return
     */
    @RequestMapping(value = "remove",method = RequestMethod.POST)
    public AppResponse remove(@RequestBody ColaCoinUserchoose userchoose){
        if (StringUtils.isBlank(userchoose.getId()) && (StringUtils.isBlank(userchoose.getCoinCode()) || StringUtils.isBlank(userchoose.getSymbol())) && StringUtils.isBlank(userchoose.getPair())){
            return AppResponse.paramsError();
        }
        String pair = userchoose.getPair();
        String[] s = pair.split("_");
        userchoose.setCoinCode(s[0]);
        userchoose.setSymbol(s[1]);
        baseBiz.remove(userchoose);
        return AppResponse.ok();
    }

    /**
     * 自选列表
     * @return
     */
    @RequestMapping(value = "list",method = RequestMethod.GET)
    public AppResponse list(){
        List<ColaCoinUserchoose> list = baseBiz.list();
        return AppResponse.ok().data(list);
    }

    /**
     * 当前自选是否存在
     * @return
     */
    @RequestMapping(value = "isFavorite")
    public AppResponse isFavorite( String pair){
        String[] s = pair.split("_");
        int i = baseBiz.isExist(s[0],s[1]);
        return AppResponse.ok().data(i);
    }

}