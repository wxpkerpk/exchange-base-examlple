package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.me.mapper.ColaSmsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

/**
 * 短信
 *
 * @author zkq
 * @create 2018-09-29 16:49
 **/
@Service
public class ColaSmsBiz {

    @Autowired
    ColaSmsMapper smsMapper;

    public List<String> getAreaCodeList(){
        return smsMapper.getAreaCodeList();
    }

    public List<Map<String,String>> getCountryList() {
        List<Map<String,String>> list =  smsMapper.getCountryList();
        List<Map<String,String>> countryList = new ArrayList<>();
        String language = "en";
        String currentLanguage = ColaLanguage.getCurrentLanguage();
        if (ColaLanguage.LANGUAGE_CN.equals(currentLanguage)){
            language = "cn";
        }
        for (Map<String, String> item : list) {
            Map<String, String> map = new HashMap<>();
            map.put("key",item.get("en"));
            map.put("value",item.get(language));
            countryList.add(map);
        }
        return countryList;
    }

    public List<Map<String, String>> countryAndAreaCode() {
        List<Map<String,String>> list =  smsMapper.countryAndAreaCode();
        List<Map<String,String>> countryList = new ArrayList<>();
        String language = "en";
        String currentLanguage = ColaLanguage.getCurrentLanguage();
        if (ColaLanguage.LANGUAGE_CN.equals(currentLanguage)){
            language = "cn";
        }
        for (Map<String, String> item : list) {
            Map<String, String> map = new HashMap<>();
            map.put("key",item.get("en"));
            map.put("areaCode",item.get("code"));
            if (ColaLanguage.LANGUAGE_CN.equals(currentLanguage)){
                map.put("country",item.get(language)+"("+item.get("en")+")");
            }else {
                map.put("country",item.get(language));
            }
            countryList.add(map);
        }
        return countryList;
    }
}
