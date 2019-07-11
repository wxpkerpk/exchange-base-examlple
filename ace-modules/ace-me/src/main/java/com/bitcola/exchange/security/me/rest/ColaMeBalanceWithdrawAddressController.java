package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaMeBalanceWithdrawAddressBiz;
import com.bitcola.me.entity.ColaMeBalanceWithdrawAddress;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("colaWithdrawAddress")
public class ColaMeBalanceWithdrawAddressController {

    @Autowired
    ColaMeBalanceWithdrawAddressBiz biz;

    @RequestMapping(value = "add",method = RequestMethod.POST)
    public AppResponse add(@RequestBody ColaMeBalanceWithdrawAddress address){
        if (StringUtils.isAnyBlank(address.getCoinCode(),address.getAddress(),address.getLabel())){
            return AppResponse.paramsError();
        }
        address.setId(UUID.randomUUID().toString());
        address.setUserId(BaseContextHandler.getUserID());
        List<ColaMeBalanceWithdrawAddress> list = biz.get(address.getCoinCode());
        if (list.size()>=5){
            biz.delete(list.get(4));
        }
        biz.insertSelective(address);
        return AppResponse.ok();
    }

    @RequestMapping("get")
    public AppResponse get(String coinCode){
        List<ColaMeBalanceWithdrawAddress> list = biz.get(coinCode);
        return AppResponse.ok().data(list);
    }

    @RequestMapping("delete")
    public AppResponse delete(String id){
        biz.deleteById(id);
        return AppResponse.ok();
    }




}