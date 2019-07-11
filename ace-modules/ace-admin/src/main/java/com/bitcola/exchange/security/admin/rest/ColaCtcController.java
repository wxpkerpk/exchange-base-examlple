package com.bitcola.exchange.security.admin.rest;

import com.bitcola.ctc.ColaCtcFee;
import com.bitcola.exchange.security.admin.biz.ColaCtcBiz;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 *
 * @author zkq
 * @create 2019-05-08 10:47
 **/
@RestController
@RequestMapping("cola/ctc")
public class ColaCtcController {

    @Autowired
    ColaCtcBiz ctcBiz;

    /**
     * 根据不同的状态请求不同的,
     * @param params
     * @return
     */
    @RequestMapping("orderList")
    public TableResultResponse orderList(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return ctcBiz.page(query);
    }

    /**
     * 接受任务,其他人无法对这个任务进行填写资料操作
     */
    @RequestMapping("acceptTask")
    public AppResponse acceptTask(String orderId){
        boolean success = ctcBiz.acceptTask(orderId);
        if (success){
            return AppResponse.ok();
        } else {
            return AppResponse.error("无法接受该任务");
        }
    }


    /**
     * 确认资金情况,填写一堆资料 (填之前,先展示信息)
     *      订单 ID
     *      银行流水号
     *
     *      amount (实际转账金额,不包含手续费)
     *
     * @return
     */
    @RequestMapping(value = "confirmAsset",method = RequestMethod.POST)
    public AppResponse confirmAsset(@RequestBody Map<String,Object> params){
        String orderId = params.get("orderId").toString();
        String pin = params.get("pin").toString();
        String bankSerialNumber = params.get("bankSerialNumber").toString();
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        if (StringUtils.isAnyBlank(orderId,bankSerialNumber,pin)){
            return AppResponse.paramsError();
        }
        if (amount.compareTo(BigDecimal.ZERO) <=0 ) return AppResponse.error("金额异常");
        String userPin = ctcBiz.getPin(BaseContextHandler.getUserID());
        if (StringUtils.isBlank(userPin)) return AppResponse.error("请先在客户端设置 pin 码");
        if (!EncoderUtil.matches(pin,userPin)){
            return AppResponse.error("密码错误");
        }
        ctcBiz.confirmAsset(orderId,bankSerialNumber,amount);
        return AppResponse.ok();
    }


    /**
     * 结束这个任务,这里扣除资金或增加资金
     * @return
     */
    @RequestMapping(value = "confirm",method = RequestMethod.POST)
    public AppResponse confirm(@RequestBody Map<String,Object> params){
        String orderId = params.get("orderId").toString();
        String pin = params.get("pin").toString();
        if (StringUtils.isBlank(orderId)) return AppResponse.paramsError();
        String userPin = ctcBiz.getPin(BaseContextHandler.getUserID());
        if (StringUtils.isBlank(userPin)) return AppResponse.error("请先在客户端设置 pin 码");
        if (!EncoderUtil.matches(pin,userPin)){
            return AppResponse.error("密码错误");
        }
        ctcBiz.confirm(orderId);
        return AppResponse.ok();
    }

    /**
     * 充值失败,或者提现失败,提现可以选择是否返还冻结的用户余额
     * @return
     */
    @RequestMapping(value = "refuse",method = RequestMethod.POST)
    public AppResponse refuse(@RequestBody Map<String,Object> params){
        String orderId = params.get("orderId").toString();
        String pin = params.get("pin").toString();
        Boolean backFrozen = (Boolean)params.get("backFrozen");
        if (StringUtils.isBlank(orderId)) return AppResponse.paramsError();
        String userPin = ctcBiz.getPin(BaseContextHandler.getUserID());
        if (StringUtils.isBlank(userPin)) return AppResponse.error("请先在客户端设置 pin 码");
        if (!EncoderUtil.matches(pin,userPin)){
            return AppResponse.error("密码错误");
        }
        boolean success = ctcBiz.refuse(orderId, backFrozen);
        if (!success) return AppResponse.error("处理失败");
        return AppResponse.ok();
    }


    /**
     * 限额,费率管理
     * @return
     */
    @RequestMapping(value = "getFeeLimit")
    public AppResponse getFeeLimit(){
        ColaCtcFee fee = ctcBiz.getFeeLimit();
        return AppResponse.ok().data(fee);
    }
    /**
     * 限额,费率管理
     * @return
     */
    @RequestMapping(value = "updateFeeLimit",method = RequestMethod.POST)
    public AppResponse updateFeeLimit(@RequestBody ColaCtcFee fee){
        if (StringUtils.isBlank(fee.getId())) return AppResponse.paramsError();
        ctcBiz.updateFeeLimit(fee);
        return AppResponse.ok();
    }

    /**
     * 订单详情
     * @return
     */
    @RequestMapping("detail")
    public AppResponse detail(String orderId){
        Map<String,Object> result = ctcBiz.detail(orderId);
        return AppResponse.ok().data(result);
    }

}
