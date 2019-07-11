package com.bitcola.exchange.bitcolapush.controller;

import com.bitcola.exchange.bitcolapush.http.Email;
import com.bitcola.exchange.bitcolapush.http.SMSUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2018-11-28 10:46
 **/
@RestController
@RequestMapping("notice")
public class NoticeController {

    @RequestMapping("warning")
    public boolean warning(String message,String telephone){
        SMSUtils.sendChinaSMS(telephone,"【BITCOLA】"+message);
        return true;
    }

    @RequestMapping("withdrawSuccessSms")
    public boolean withdrawSuccessSms(@RequestParam("areaCode")String areaCode, @RequestParam("telephone")String telephone, @RequestParam("coinCode")String coinCode, @RequestParam("number")String number,@RequestParam("realNumber")String realNumber){
        String content = "[BITCOLA] Dear users, the %s %s you have withdraw has been remitted, and the actual amount arrived is %s %s. Thank you for choosing bitcola.io!";
        if (SMSUtils.CHINA_CODE.equals(areaCode)){
            content = "【BitCola】 尊敬的用户，您提现的%s %s 已汇出，实际到账金额为 %s %s 。感谢您选择BitCola！";
        }
        content = String.format(content, number,coinCode,realNumber,coinCode);
        SMSUtils.sendSMS(areaCode,telephone,content);
        return true;
    }


    @RequestMapping("withdrawSuccessEmail")
    public boolean withdrawSuccessEmail(@RequestParam("email")String email, @RequestParam("coinCode")String coinCode, @RequestParam("number")String number,@RequestParam("realNumber")String realNumber,String language,String fishCode){
        String text = "withdraw number : "+number+",real number : "+realNumber;
        if ("CN".equalsIgnoreCase(language)){
            text = "提币数量: "+number+", 实际到账: "+realNumber;
        }
        Email.sendEmail(email,text,language,fishCode,"BITCOLA successfully withdraw "+coinCode,"BitCola 成功提币"+coinCode);
        return true;
    }

    @RequestMapping("depositSuccessSms")
    public boolean depositSuccessSms(@RequestParam("areaCode")String areaCode, @RequestParam("telephone")String telephone, @RequestParam("coinCode")String coinCode, @RequestParam("number")String number){
        String content = "[BitCola] Dear users, your recharged %s %s has arrived. Thank you for choosing bitcola.io!";
        if (SMSUtils.CHINA_CODE.equals(areaCode)){
            content = "【BitCola】尊敬的用户，您充值的%s %s已到账。感谢您选择BitCola！";
        }
        content = String.format(content, number,coinCode);
        SMSUtils.sendSMS(areaCode,telephone,content);
        return true;
    }

    @RequestMapping("depositSuccessEmail")
    public boolean depositSuccessEmail(@RequestParam("email")String email, @RequestParam("coinCode")String coinCode, @RequestParam("number")String number,String language,String fishCode){
        String content = "[BitCola] Dear users, your recharged %s %s has arrived. Thank you for choosing bitcola.io!";
        if ("CN".equalsIgnoreCase(language)){
            content = "【BitCola】尊敬的用户，您充值的%s %s已到账。感谢您选择bitcola.io！";
        }
        content = String.format(content, number,coinCode);
        Email.sendEmail(email,content,language,fishCode,"BITCOLA successfully deposit "+coinCode,"BitCola 成功充值"+coinCode);
        return true;
    }




}
