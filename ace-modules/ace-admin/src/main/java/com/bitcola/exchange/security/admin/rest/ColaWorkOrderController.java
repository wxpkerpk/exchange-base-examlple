package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaUserBiz;
import com.bitcola.exchange.security.admin.biz.ColaWordOrderBiz;
import com.bitcola.exchange.security.admin.mapper.ColaWorkOrderDetailMapper;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.me.entity.ColaMeWorkOrder;
import com.bitcola.me.entity.ColaMeWorkOrderDetail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author zkq
 * @create 2018-12-24 12:14
 **/
@RestController
@RequestMapping("cola/wordOrder")
public class ColaWorkOrderController extends BaseController<ColaWordOrderBiz, ColaMeWorkOrder> {

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    ColaWorkOrderDetailMapper detailMapper;

    /**
     * 完成
     * @return
     */
    @RequestMapping(value = "complete",method = RequestMethod.POST)
    public AppResponse complete(@RequestBody ColaMeWorkOrder order){
        if (StringUtils.isBlank(order.getId())){
            return AppResponse.paramsError();
        }
        ColaMeWorkOrder colaMeWorkOrder = baseBiz.selectById(order.getId());
        colaMeWorkOrder.setStatus("Processed");
        baseBiz.updateById(colaMeWorkOrder);
        return AppResponse.ok();
    }

    /**
     * 回复
     * @return
     */
    @RequestMapping(value = "reply",method = RequestMethod.POST)
    public AppResponse reply(@RequestBody ColaMeWorkOrderDetail detail){
        if (StringUtils.isBlank(detail.getContent()) || StringUtils.isBlank(detail.getOrderId())){
            return AppResponse.paramsError();
        }
        ColaMeWorkOrder colaMeWorkOrder = baseBiz.selectById(detail.getOrderId());
        if (colaMeWorkOrder.getStatus().equals("Processed")){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_WORD_ORDER_COMPLETE));
        }
        detail.setId(UUID.randomUUID().toString());
        detail.setTime(System.currentTimeMillis());
        detail.setUserId(BaseContextHandler.getUserID());
        detail.setUsername(BaseContextHandler.getUsername());
        detailMapper.insertSelective(detail);
        if (colaMeWorkOrder.getStatus().equals("Unprocessed")){
            colaMeWorkOrder.setStatus("Processing");
            baseBiz.updateById(colaMeWorkOrder);
        }
        return AppResponse.ok();
    }



}
