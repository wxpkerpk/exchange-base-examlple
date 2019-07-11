package com.bitcola.exchange.security.me.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 订单 id 生成
 *
 * @author zkq
 * @create 2018-10-23 16:25
 **/
@Component
public class OrderIdUtil {

    private SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");

    @Autowired
    SequenceFactory sequenceFactory;

    /**
     * 生成带日期的13位订单
     * @param key 具体是啥订单 (充值,提现...)
     * @return
     */
    public String getId(String key){
        String format = sdf.format(new Date());
        Long generate = sequenceFactory.generate(key+format, 1, TimeUnit.DAYS);
        int length = generate.toString().length();
        for (int i = 0; i < 5-length; i++){
            format += "0";
        }
        format += generate.toString();
        return format;
    }


}
