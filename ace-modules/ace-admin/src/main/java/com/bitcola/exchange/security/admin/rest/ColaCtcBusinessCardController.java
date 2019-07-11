package com.bitcola.exchange.security.admin.rest;

import com.bitcola.ctc.ColaCtcBankCard;
import com.bitcola.exchange.security.admin.biz.ColaCtcBankCardBiz;
import com.bitcola.exchange.security.admin.biz.ColaCtcBusinessCardBiz;
import com.bitcola.exchange.security.admin.entity.ColaCtcBusinessCard;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2019-05-07 15:07
 **/
@RestController
@RequestMapping("cola/ctc/business")
public class ColaCtcBusinessCardController extends BaseController<ColaCtcBusinessCardBiz, ColaCtcBusinessCard> {

    @Autowired
    ColaCtcBankCardBiz bankCardBiz;

    @RequestMapping("detail")
    public AppResponse detail(String cardId){
        ColaCtcBankCard colaCtcBankCard = bankCardBiz.selectById(cardId);
        return AppResponse.ok().data(colaCtcBankCard);
    }

    @RequestMapping(value = "insert",method = RequestMethod.POST)
    public AppResponse insert(@RequestBody ColaCtcBankCard bankCard){
        baseBiz.insert(bankCard);
        return AppResponse.ok();
    }

    @RequestMapping(value = "delete",method = RequestMethod.POST)
    public AppResponse delete(@RequestBody ColaCtcBusinessCard businessCard){
        baseBiz.deleteById(businessCard.getCardId());
        bankCardBiz.deleteById(businessCard.getCardId());
        return AppResponse.ok();
    }

    @RequestMapping(value = "updateAvailable",method = RequestMethod.POST)
    public AppResponse updateAvailable(@RequestBody ColaCtcBusinessCard businessCard){
        baseBiz.updateSelectiveById(businessCard);
        return AppResponse.ok();
    }

}
