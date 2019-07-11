//package com.bitcola.exchange.security.me.rest;
//
//import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
//import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
//import com.bitcola.exchange.security.common.msg.AppResponse;
//import com.bitcola.exchange.security.me.biz.ColaIcoDepositBiz;
//import com.bitcola.exchange.security.me.biz.ColaIcoUserBiz;
//import com.bitcola.exchange.security.me.constant.AuditStatusConstant;
//import com.bitcola.exchange.security.me.vo.DepositVo;
//import com.bitcola.me.entity.ColaIcoUser;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//import tk.mybatis.mapper.entity.Example;
//
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
///**
// * ICO 接口
// *
// * @author zkq
// * @create 2018-09-29 09:45
// **/
//@RestController
//@RequestMapping("ico")
//public class ColaIcoController {
//
//    @Autowired
//    ColaIcoUserBiz icoUserBiz;
//
//    @Autowired
//    ColaIcoDepositBiz depositBiz;
//
//    @RequestMapping(value = "getAllIcoUser",method = RequestMethod.GET)
//    public AppResponse getAllIcoUser(){
//        Example example = new Example(ColaIcoUser.class);
//        example.orderBy("userId").desc();
//        return AppResponse.ok().data(icoUserBiz.selectByExample(example));
//    }
//
//
//
//    /**
//     * 后台审核 ICO 资料
//     * @return
//     */
//    @RequestMapping(value = "audit",method = RequestMethod.POST)
//    public AppResponse audit(@RequestBody Map<String,String> params){
//        String id = params.get("id");
//        String option = params.get("option");
//        if (StringUtils.isBlank(id) || StringUtils.isBlank(option)){
//            return AppResponse.paramsError();
//        }
//        if (!AuditStatusConstant.AGREE.equals(option) && !AuditStatusConstant.REFUSE.equals(option)){
//            return AppResponse.paramsError();
//        }
//        icoUserBiz.audit(id,option);
//        return AppResponse.ok();
//    }
//
//
//
//    /**
//     * ico 截止时间 和剩余时间
//     * @return
//     */
//    @RequestMapping("getDeadlineAndSellPercent")
//    @IgnoreUserToken
//    public AppResponse getDeadlineAndSellPercent(){
//        Map<String,Object> map = icoUserBiz.getDeadlineAndSellPercent();
//        return AppResponse.ok().data(map);
//    }
//
//
//    /**
//     * 服务器当前时间
//     * @return
//     */
//    @RequestMapping("getCurrentTime")
//    @IgnoreUserToken
//    public AppResponse getCurrentTime(){
//        return AppResponse.ok().data(System.currentTimeMillis());
//    }
//
//    /**
//     * 用户资料提交审核
//     * @param info
//     * @return
//     */
//    @RequestMapping( value = "userInfoSubmit",method = RequestMethod.POST)
//    public AppResponse userInfoSubmit(@RequestBody ColaIcoUser info){
//        if (StringUtils.isBlank(info.getAreaCode()) || StringUtils.isBlank(info.getBackSide()) || StringUtils.isBlank(info.getBirthday()) ||
//                StringUtils.isBlank(info.getCountry()) || StringUtils.isBlank(info.getEmail()) ||
//                StringUtils.isBlank(info.getFirstName()) || StringUtils.isBlank(info.getFrontSide()) || StringUtils.isBlank(info.getIdCardType()) ||
//                StringUtils.isBlank(info.getLastName()) || StringUtils.isBlank(info.getGender()) || StringUtils.isBlank(info.getTelPhone()) ||
//                info.getPlannedInvestment() == null) {
//            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_ICO_INFO_ERROR));
//        }
//        // 检测 eth 地址是否存在  -- 采用 BTC ,不校验地址,使用充值地址判断用户充值
//        //boolean b = icoUserBiz.checkAddress(info.getAddress());
//        //if (b){
//        //    return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_ICO_ADDRESS_ERROR));
//        //}
//        icoUserBiz.userInfoSubmit(info);
//
//        return AppResponse.ok();
//    }
//
//    /**
//     *  ico 地址 (user)
//     * @return
//     */
//    @RequestMapping("icoInfo")
//    public AppResponse icoInfo(){
//        ColaIcoUser user = icoUserBiz.icoInfo();
//        Map<String,Object> map = new HashMap<>();
//        map.put("checkStatus",user.getCheckStatus());
//        if (user.getCheckStatus() == 1){
//            map.put("depositAddress",user.getDepositAddress());
//        }
//        BigDecimal number = icoUserBiz.colaTokenIcoTotalNumber();
//        map.put("colaTokenSaleStatus",0);
//        if (number.compareTo(new BigDecimal(icoUserBiz.getIcoConfig("ico_total_number"))) > 0){
//            map.put("colaTokenSaleStatus",-1);
//            map.remove("depositAddress");
//        }
//        if (System.currentTimeMillis() > Long.valueOf(icoUserBiz.getIcoConfig("ico_deadline"))){
//            map.put("colaTokenSaleStatus", -2);
//            map.remove("depositAddress");
//        }
//        return AppResponse.ok().data(map);
//    }
//
//    /**
//     * 已经购买多少token (deposit)
//     */
//    @RequestMapping("colaTokenNumber")
//    public AppResponse colaTokenNumber(){
//        BigDecimal num = icoUserBiz.colaTokenNumber();
//        if (num == null){
//            num = BigDecimal.ZERO;
//        }
//        return AppResponse.ok().data(num);
//    }
//
//    /**
//     * token信息,包含 以太坊价格,奖励比例(api,取币安, zb, okex 平均值)
//     * @return
//     */
//    @RequestMapping("colaTokenInfo")
//    public AppResponse colaTokenInfo(){
//        Map<String,Object> map = icoUserBiz.colaTokenInfo();
//        return AppResponse.ok().data(map);
//    }
//
//    /**
//     * 通过状态(通过,拒绝,等待,没有进行申请) (user)
//     * @return
//     */
//    @RequestMapping("icoStatus")
//    public AppResponse icoStatus(){
//        Integer status = icoUserBiz.icoStatus();
//        return AppResponse.ok().data(status);
//    }
//
//    /**
//     * 充值列表
//     * @return
//     */
//    @RequestMapping("depositList")
//    public AppResponse depositList(){
//        List<DepositVo> list = depositBiz.depositList();
//        return AppResponse.ok().data(list);
//    }
//
//    /**
//     * 订阅
//     * @return
//     */
//    @IgnoreUserToken
//    @RequestMapping("subscribe")
//    public AppResponse subscribe(String email){
//        if (StringUtils.isBlank(email)){
//            return AppResponse.paramsError();
//        }
//        if(icoUserBiz.checkSubscribeExist(email)){
//            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_ICO_SUBSCRIBE));
//        }
//        icoUserBiz.subscribe(email);
//        return AppResponse.ok();
//    }
//
//
//
//}
