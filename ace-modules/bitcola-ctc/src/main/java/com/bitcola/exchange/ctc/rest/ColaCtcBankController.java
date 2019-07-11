package com.bitcola.exchange.ctc.rest;

import com.bitcola.ctc.ColaCtcBankCard;
import com.bitcola.exchange.ctc.biz.ColaCtcBankCardBiz;
import com.bitcola.exchange.ctc.biz.ColaUserBiz;
import com.bitcola.exchange.ctc.constant.CtcResponseCode;
import com.bitcola.exchange.ctc.entity.ColaUser;
import com.bitcola.exchange.ctc.util.BankCardVerify;
import com.bitcola.exchange.ctc.util.CardIdVerify;
import com.bitcola.exchange.ctc.util.TCaptchaVerify;
import com.bitcola.exchange.ctc.vo.BankCardAddParams;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加和选择银行卡
 * @author zkq
 * @create 2019-05-07 16:24
 **/
@Log4j2
@RestController
@RequestMapping("bank")
public class ColaCtcBankController {

    @Autowired
    ColaCtcBankCardBiz biz;

    @Autowired
    ColaUserBiz userBiz;

    /**
     * 银行列表
     * @return
     */
    @RequestMapping("bankList")
    public AppResponse bankList(){
        List<Map<String,String>> result = biz.bankList();
        return AppResponse.ok().data(result);
    }

    /**
     * 核实用户是否进行 KYC ,核验实名认证时的身份证号是否正确,不正确返回提示, 正确时返回真实姓名和身份证号
     * @return
     */
    @RequestMapping("checkUser")
    public AppResponse checkUser(){
        String userID = BaseContextHandler.getUserID();
        // 检测是否有绑定银行卡
        List<ColaCtcBankCard> cards = biz.list(userID);
        if (cards.size() >= 1) return AppResponse.error(CtcResponseCode.CTC_HAVE_BANK_CARD_ERROR,CtcResponseCode.CTC_HAVE_BANK_CARD_ERROR_MSG);
        // 检测是否进行 kyc
        ColaUser userInfo = userBiz.getUserInfo(userID);
        if (userInfo.getKycStatus() == null || userInfo.getKycStatus() != 1) return AppResponse.error(CtcResponseCode.CTC_NO_KYC,ResponseCode.NOT_PASS_KYC_MESSAGE);
        if (StringUtils.isBlank(userInfo.getPin())) return AppResponse.error(CtcResponseCode.CTC_NO_PIN,ResponseCode.NO_MONEY_PASSWORD_MESSAGE);
        //  校验身份证号
        if (!CardIdVerify.verify(userInfo.getDocumentNumber())) return AppResponse.error(CtcResponseCode.CTC_KYC_NUMBER_ERROR,CtcResponseCode.CTC_KYC_NUMBER_ERROR_MSG);
        Map<String,Object> map = new HashMap<>();
        map.put("username",userInfo.getLastName()+userInfo.getFirstName());
        map.put("documentNumber",dealCardId(userInfo.getDocumentNumber()));
        return AppResponse.ok().data(map);
    }

    /**
     * 绑定银行卡
     * @return
     */
    @RequestMapping(value = "add",method = RequestMethod.POST)
    public AppResponse add(@RequestBody BankCardAddParams params,HttpServletRequest request){
        if (StringUtils.isAnyBlank(params.getTicket(), params.getRand())){
            return AppResponse.paramsError();
        }
        int i = TCaptchaVerify.verifyTicket(params.getTicket(), params.getRand(), getIp(request));
        if (i == -1) {
            log.error("防水墙错误");
            //todo 暂时关闭防水墙 return AppResponse.error(ColaLanguage.get(ColaLanguage.VERIFY_FAILED));
        } else if (i <= 95){
            log.info("防水墙验证成功");
        } else {
            log.error("防水墙验证拦截 恶意等级:"+i);
            return AppResponse.error(ColaLanguage.get(ColaLanguage.VERIFY_FAILED));
        }
        String userID = BaseContextHandler.getUserID();
        // 检测是否有绑定银行卡
        List<ColaCtcBankCard> cards = biz.list(userID);
        if (cards.size() >= 1) return AppResponse.error(CtcResponseCode.CTC_HAVE_BANK_CARD_ERROR,CtcResponseCode.CTC_HAVE_BANK_CARD_ERROR_MSG);
        // 检测是否进行 kyc
        ColaUser userInfo = userBiz.getUserInfo(userID);
        if (userInfo.getKycStatus() == null || userInfo.getKycStatus() != 1) return AppResponse.error(CtcResponseCode.CTC_NO_KYC,ResponseCode.NOT_PASS_KYC_MESSAGE);
        if (StringUtils.isBlank(userInfo.getPin())) return AppResponse.error(CtcResponseCode.CTC_NO_PIN,ResponseCode.NO_MONEY_PASSWORD_MESSAGE);
        //  校验身份证号
        String documentNumber = userInfo.getDocumentNumber();
        if (!CardIdVerify.verify(documentNumber)) return AppResponse.error(CtcResponseCode.CTC_KYC_NUMBER_ERROR,CtcResponseCode.CTC_KYC_NUMBER_ERROR_MSG);
        String realName = userInfo.getLastName() + userInfo.getFirstName();
        if (StringUtils.isAnyBlank(params.getCardId(),params.getBankId(),params.getBankAddress(),params.getPin())){
            return AppResponse.paramsError();
        }
        params.setCardId(params.getCardId().trim());
        // 验证 pin
        boolean matches = EncoderUtil.matches(params.getPin(), userInfo.getPin());
        if (!matches) return AppResponse.error(ResponseCode.PIN_ERROR_CODE,ResponseCode.PIN_ERROR_MESSAGE);
        String checkMessage = biz.checkBankCard(params.getCardId(),realName,documentNumber);
        if (StringUtils.isNotBlank(checkMessage)) return AppResponse.error(CtcResponseCode.CTC_BANK_CARD_ERROR,checkMessage);

        String bankId = params.getBankId();
        Map<String,String> bank = biz.getBankInfo(bankId);

        ColaCtcBankCard bankCard = new ColaCtcBankCard();
        bankCard.setChecked(1);
        bankCard.setUserName(realName);
        bankCard.setDocumentNumber(documentNumber);
        bankCard.setUserId(userID);
        bankCard.setBankAddress(params.getBankAddress());
        bankCard.setBankName(bank.get("bank"));
        bankCard.setIcon(bank.get("icon"));
        bankCard.setWhiteIcon(bank.get("white"));
        bankCard.setCardId(params.getCardId());
        bankCard.setSign(BankCardVerify.sign(bankCard.getCardId(),bankCard.getUserName(),bankCard.getUserId(),bankCard.getDocumentNumber()));
        String message = biz.add(bankCard);
        if (StringUtils.isNotBlank(checkMessage)) return AppResponse.error(CtcResponseCode.CTC_BANK_CARD_ERROR,message);
        return AppResponse.ok();
    }

    /**
     * 自己的银行卡列表
     * 信息模糊化处理
     * @return
     */
    @RequestMapping(value = "list",method = RequestMethod.GET)
    public AppResponse list(){
        List<ColaCtcBankCard> cards = biz.list(BaseContextHandler.getUserID());
        for (ColaCtcBankCard card : cards) {
            card.setUserName("*"+card.getUserName().substring(1));
            card.setCardId(dealCardId(card.getCardId()));
            card.setDocumentNumber(dealCardId(card.getDocumentNumber()));
        }
        return AppResponse.ok().data(cards);
    }

    /**
     * 解绑银行卡
     *   将银行卡用户 ID 设置为 null
     *
     * @return
     */
    @RequestMapping(value = "Untying",method = RequestMethod.POST)
    public AppResponse Untying(@RequestBody Map<String,String> params){
        String userID = BaseContextHandler.getUserID();
        String pin = params.get("pin");
        if (StringUtils.isBlank(pin)) return AppResponse.paramsError();
        ColaUser userInfo = userBiz.getUserInfo(userID);
        boolean matches = EncoderUtil.matches(pin, userInfo.getPin());
        if (!matches) return AppResponse.error(ResponseCode.PIN_ERROR_CODE,ResponseCode.PIN_ERROR_MESSAGE);
        List<ColaCtcBankCard> cards = biz.list(userID);
        if (cards.size() == 0) return AppResponse.error("您未绑定银行卡。");
        ColaCtcBankCard bankCard = cards.get(0);
        bankCard.setUserId(null);
        biz.updateById(bankCard);
        return AppResponse.ok();
    }

    private String dealCardId(String cardId){
        String prefix = cardId.substring(0,4);
        String suffix = cardId.substring(cardId.length()-4);
        int len = cardId.length() - 8;
        StringBuilder mid = new StringBuilder();
        for (int i = 0; i < len; i++) {
            mid.append("*");
        }
        return prefix+mid.append(suffix).toString();
    }

    private String getIp(HttpServletRequest req){
        String ip = req.getHeader("x-user-ip");
        if (StringUtils.isBlank(ip)){
            ip = req.getHeader("x-real-ip");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("x-forwarded-for");
            if (ip != null && ip.split(",").length>1){
                ip = ip.split(",")[0];
            }
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }

}
