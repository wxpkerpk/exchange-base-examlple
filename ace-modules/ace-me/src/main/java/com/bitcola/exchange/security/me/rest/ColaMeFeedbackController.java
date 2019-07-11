package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.exchange.security.me.biz.ColaMeFeedbackBiz;
import com.bitcola.me.entity.ColaMeFeedback;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("colaMeFeedback")
public class ColaMeFeedbackController extends BaseController<ColaMeFeedbackBiz,ColaMeFeedback> {


    /**
     * 新增一条意见
     * @param entity
     * @return
     */
    @RequestMapping("insert")
    public AppResponse insert(ColaMeFeedback entity){
        if (StringUtils.isBlank(entity.getContent())){
            return  AppResponse.paramsError();
        }
        String id = UUID.randomUUID().toString().replace("-", "");
        String userID = BaseContextHandler.getUserID();
        entity.setDate(System.currentTimeMillis());
        entity.setId(id);
        entity.setUserId(userID);
        baseBiz.insert(entity);
        return new AppResponse();
    }

    /**
     * 意见列表
     *
     * @author zkq
     * @date 2018/7/15 16:13
     * @return com.bitcola.exchange.security.common.msg.BaseResponse
     */
    @RequestMapping("list")
    public AppResponse list(){
        List<ColaMeFeedback> list = baseBiz.list(BaseContextHandler.getUserID());
        AppResponse resp = new AppResponse();
        resp.setData(list);
        return resp;
    }
}