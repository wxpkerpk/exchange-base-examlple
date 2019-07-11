package com.bitcola.exchange.security.admin.rest;

import com.bitcola.ctc.ColaCtcBankCard;
import com.bitcola.exchange.security.admin.biz.ColaBankBiz;
import com.bitcola.exchange.security.admin.biz.ColaCtcBankCardBiz;
import com.bitcola.exchange.security.admin.entity.ColaBank;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;
import java.util.UUID;

/**
 * 银行相关
 * @author zkq
 * @create 2019-05-07 15:07
 **/
@RestController
@RequestMapping("cola/ctc/bank")
public class ColaCtcBankCardController extends BaseController<ColaCtcBankCardBiz, ColaCtcBankCard> {

    @Autowired
    ColaBankBiz bankBiz;

    /**
     * 银行列表
     * @return
     */
    @RequestMapping("bankList")
    public AppResponse bankList(){
        List<ColaBank> banks = bankBiz.selectListAll();
        return AppResponse.ok().data(banks);
    }

    /**
     * 修改银行
     * @return
     */
    @RequestMapping(value = "updateBank",method = RequestMethod.POST)
    public AppResponse updateBank(@RequestBody ColaBank bank){
        if (StringUtils.isBlank(bank.getId())) return AppResponse.paramsError();
        bankBiz.updateSelectiveById(bank);
        return AppResponse.ok();
    }
    /**
     * 删除银行
     * @return
     */
    @RequestMapping(value = "deleteBank",method = RequestMethod.POST)
    public AppResponse deleteBank(@RequestBody ColaBank bank){
        if (StringUtils.isBlank(bank.getId())) return AppResponse.paramsError();
        bankBiz.deleteById(bank.getId());
        return AppResponse.ok();
    }
    /**
     * 添加银行
     * @return
     */
    @RequestMapping(value = "addBank",method = RequestMethod.POST)
    public AppResponse addBank(@RequestBody ColaBank bank){
        if (StringUtils.isAnyBlank(bank.getBankName(),bank.getIcon(),bank.getWhiteIcon()))
            return AppResponse.paramsError();
        bank.setId(UUID.randomUUID().toString());
        bankBiz.insertSelective(bank);
        return AppResponse.ok();
    }

}
