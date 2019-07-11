package com.bitcola.exchange.security.me.rest;


import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.exchange.security.me.biz.ColaMeWorkOrderBiz;
import com.bitcola.exchange.security.me.biz.ColaMeWorkOrderDetailBiz;
import com.bitcola.exchange.security.me.biz.ColaUserBiz;
import com.bitcola.exchange.security.me.constant.WorkOrderConstant;
import com.bitcola.exchange.security.me.vo.WorkOrderDetailVo;
import com.bitcola.me.entity.ColaMeWorkOrder;
import com.bitcola.me.entity.ColaMeWorkOrderDetail;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("colaMeWorkOrder")
public class ColaMeWorkOrderController {

    @Autowired
    ColaMeWorkOrderBiz biz;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    ColaMeWorkOrderDetailBiz workOrderDetailBiz;

    /**
     * 创建工单
     * @return
     */
    @RequestMapping(value = "create",method = RequestMethod.POST)
    public AppResponse create(@RequestBody ColaMeWorkOrder order){
        if (StringUtils.isBlank(order.getCoinCode()) || StringUtils.isBlank(order.getTitle()) || StringUtils.isBlank(order.getContent()) || StringUtils.isBlank(order.getType())){
            return AppResponse.paramsError();
        }
        ColaUserEntity info = userBiz.info(BaseContextHandler.getUserID());
        order.setFromNickName(info.getNickName());
        order.setFromUserId(BaseContextHandler.getUserID());
        order.setFromUsername(BaseContextHandler.getUsername());
        order.setId(UUID.randomUUID().toString());
        order.setStatus(WorkOrderConstant.STATUS_SUBMISSION);
        order.setTime(System.currentTimeMillis());
        biz.insert(order);
        return AppResponse.ok();
    }

    /**
     * 列表
     * @return
     */
    @RequestMapping("list")
    public AppResponse list(Long timestamp,Integer size){
        if (timestamp == null || timestamp == 0){
            timestamp = System.currentTimeMillis();
        }
        if (size == null || size == 0){
            size = 15;
        }
        List<ColaMeWorkOrder> list = biz.list(timestamp,size);
        return AppResponse.ok().data(list);
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
        ColaMeWorkOrder colaMeWorkOrder = biz.selectById(detail.getOrderId());
        if (colaMeWorkOrder.getStatus().equals(WorkOrderConstant.STATUS_COMPLETE)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_WORD_ORDER_COMPLETE));
        }
        ColaUserEntity info = userBiz.info(BaseContextHandler.getUserID());
        detail.setNickName(info.getNickName());
        detail.setId(UUID.randomUUID().toString());
        detail.setTime(System.currentTimeMillis());
        detail.setUserId(BaseContextHandler.getUserID());
        detail.setUsername(BaseContextHandler.getUsername());
        workOrderDetailBiz.insert(detail);
        if (colaMeWorkOrder.getStatus().equals(WorkOrderConstant.STATUS_SUBMISSION)){
            colaMeWorkOrder.setStatus(WorkOrderConstant.STATUS_REPLYING);
            biz.updateById(colaMeWorkOrder);
        }
        return AppResponse.ok();
    }



    /**
     * 工单详情
     * @param id
     * @return
     */
    @RequestMapping("detail")
    public AppResponse detail(String id){
        if (StringUtils.isBlank(id)) return AppResponse.paramsError();
        ColaMeWorkOrder colaMeWorkOrder = biz.selectById(id);
        ColaMeWorkOrderDetail title = new ColaMeWorkOrderDetail();
        title.setId(id);
        title.setOrderId(id);
        title.setTime(colaMeWorkOrder.getTime());
        title.setUserId(colaMeWorkOrder.getFromUserId());
        title.setUsername(colaMeWorkOrder.getFromUsername());
        title.setContent(colaMeWorkOrder.getContent());
        title.setImages(colaMeWorkOrder.getImages());
        title.setImageList(colaMeWorkOrder.getImageList());
        title.setNickName(colaMeWorkOrder.getFromNickName());
        List<ColaMeWorkOrderDetail> colaMeWorkOrderDetails = new ArrayList<>();
        colaMeWorkOrderDetails.add(title);

        ColaMeWorkOrderDetail order = new ColaMeWorkOrderDetail();
        order.setOrderId(id);
        List<ColaMeWorkOrderDetail> item = workOrderDetailBiz.selectList(order);
        if (item.size()!=0){
            colaMeWorkOrderDetails.addAll(item);
        }
        return AppResponse.ok().data(colaMeWorkOrderDetails);
    }
}