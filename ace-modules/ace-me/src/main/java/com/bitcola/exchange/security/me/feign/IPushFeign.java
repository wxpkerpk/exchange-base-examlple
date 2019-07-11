package com.bitcola.exchange.security.me.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-14 10:12
 **/
@FeignClient(value = "bitcola-push")
@Repository
public interface IPushFeign {

    @RequestMapping(value = "ip/getAddress",method = RequestMethod.GET)
    public String getAddress(@RequestParam("ip") String ip);

    @RequestMapping(value = "price/eos",method = RequestMethod.GET)
    public BigDecimal eos();

    @RequestMapping(value = "price/btc",method = RequestMethod.GET)
    public BigDecimal btc();

    @RequestMapping(value = "price/eth",method = RequestMethod.GET)
    public BigDecimal eth();

    @RequestMapping(value = "currency/rate",method = RequestMethod.GET)
    public Map<String,BigDecimal> currency();

    @RequestMapping(value = "captcha/sms",method = RequestMethod.GET)
    public void sms(@RequestParam("captcha")String captcha,@RequestParam("areaCode")String areaCode,@RequestParam("telephone")String telephone);


    @RequestMapping(value = "captcha/email",method = RequestMethod.GET)
    public void email(@RequestParam("language")String language,@RequestParam("to")String to,@RequestParam("code")String code,@RequestParam("antiPhishingCode")String antiPhishingCode);

    @RequestMapping("dingxiang/verifyToken")
    public boolean verifyToken(@RequestParam("token") String token);

    @RequestMapping(value = "notice/warning",method = RequestMethod.GET)
    void smsWarning(@RequestParam("message") String message,@RequestParam("telephone")String telephone);

    @RequestMapping(value = "notice/withdrawSuccessSms",method = RequestMethod.GET)
    boolean withdrawSuccessSms(@RequestParam("areaCode")String areaCode, @RequestParam("telephone")String telephone, @RequestParam("coinCode")String coinCode, @RequestParam("number")String number,@RequestParam("realNumber")String realNumber);
    @RequestMapping(value = "notice/withdrawSuccessEmail",method = RequestMethod.GET)
    boolean withdrawSuccessEmail(@RequestParam("email") String email, @RequestParam("coinCode") String coinCode, @RequestParam("number") String number, @RequestParam("realNumber") String realNumber, @RequestParam("language") String language, @RequestParam("fishCode")String fishCode);

    @RequestMapping(value = "user/getAvatar",method = RequestMethod.GET)
    public String getAvatar(@RequestParam("nickName")String nickName);

}
