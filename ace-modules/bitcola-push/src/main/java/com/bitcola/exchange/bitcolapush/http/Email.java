package com.bitcola.exchange.bitcolapush.http;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.builder.SendCloudBuilder;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.core.SendCloud;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.model.MailBody;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.model.SendCloudMail;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.model.TemplateContent;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.util.ResponseData;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author zkq
 * @create 2018-12-11 18:06
 **/
public class Email {
    public static final String url = "http://api.sendcloud.net/apiv2/mail/sendtemplate";
    public static final String apiUser = "bitcola";
    public static final String apiKey = "BwOI2rYkJZG3NPqV";

    public static void main(String[] args) throws IOException {
        sendEmail("zhangkaiqiu888@gmail.com","提币数量 : 10, 实际到账 : 9","CN","acdgf","BITCOLA verification code","BitCola 成功提币通知");
    }

    public static boolean sendEmail(String toEmail,String captcha,String language,String anitFishCode,String titleEN,String titleCN) {
        MailBody body = new MailBody();
        // 设置 From
        body.setFrom("service@mail.bitcola.io");
        // 设置 FromName
        body.setFromName("BITCOLA");
        body.setReplyTo(toEmail);
        String anitFish = "";
        Map<String,List<String>> sub  = new HashMap<>(7);
        if ("CN".equals(language)){
            body.setSubject(titleCN);
            if (StringUtils.isNotBlank(anitFishCode)){
                anitFish = "防钓鱼码："+anitFishCode;
            }
            //sub = new Template("BitCola验证码",captcha,"感谢您选择 bitcola.io","BitCola 官方","联系我们","邮件",anitFish);

            sub.put("%title%",Arrays.asList(titleCN));
            sub.put("%codes%",Arrays.asList(captcha));
            sub.put("%thank%",Arrays.asList("感谢您选择 bitcola.io"));
            sub.put("%official%",Arrays.asList("BitCola 官方"));
            sub.put("%concat%",Arrays.asList("联系我们"));
            sub.put("%email%",Arrays.asList("邮件"));
            sub.put("%phish%",Arrays.asList(anitFish));


        } else {
            body.setSubject(titleEN);
            if (StringUtils.isNotBlank(anitFishCode)){
                anitFish = "Anti-phishing code："+anitFishCode;
            }
            //sub = new Template("BITCOLA verification code",captcha,"Thank you for your choice bitcola.io","BitCola Official","contact us","email",anitFish);
            sub.put("%title%",Arrays.asList(titleEN));
            sub.put("%codes%",Arrays.asList(captcha));
            sub.put("%thank%",Arrays.asList("Thank you for your choice bitcola.io"));
            sub.put("%official%",Arrays.asList("BitCola Official"));
            sub.put("%concat%",Arrays.asList("contact us"));
            sub.put("%email%",Arrays.asList("email"));
            sub.put("%phish%",Arrays.asList(anitFish));
        }

        List<String> toList = new ArrayList<String>();
        toList.add(toEmail);
        body.addXsmtpapi("to", toList);
        body.addXsmtpapi("sub", sub );
        System.out.println(JSONObject.toJSONString(sub));
        // 使用邮件模板
        TemplateContent content = new TemplateContent();
        content.setTemplateInvokeName("captcha");

        SendCloudMail mail = new SendCloudMail();
        // 模板发送时, 必须使用 Xsmtpapi 来指明收件人; mail.setTo();
        mail.setBody(body);
        mail.setContent(content);

        SendCloud sc = SendCloudBuilder.build();
        try {
            ResponseData res = sc.sendMail(mail);
            if (res.result) {
                return true;
            } else {
                System.out.println(res.getMessage());
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return true;
    }



    @Data
    static class Template{
        List<String> title;
        List<String> codes;
        List<String> thank;
        List<String> official;
        List<String> concat;
        List<String> email;
        List<String> phish;
        Template(String title,
                String codes,
                String thank,
                String official,
                String concat,
                String email,
                String phish){
            this.title = Arrays.asList(title);
            this.codes = Arrays.asList(codes);
            this.thank = Arrays.asList(thank);
            this.official = Arrays.asList(official);
            this.concat = Arrays.asList(concat);
            this.email = Arrays.asList(email);
            this.phish = Arrays.asList(phish);
        }
    }
}
