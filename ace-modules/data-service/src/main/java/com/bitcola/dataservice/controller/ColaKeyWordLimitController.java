package com.bitcola.dataservice.controller;

import com.bitcola.dataservice.util.SensitiveWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2018-11-14 17:07
 **/
@RestController
@RequestMapping("keyWordLimit")
public class ColaKeyWordLimitController {

    @Autowired
    SensitiveWord sensitiveWord;

    @RequestMapping("contain")
    public boolean contain(String str){
        if(sensitiveWord.contain(str)){
            return true;
        }
        return false;
    }

    @RequestMapping("replace")
    public String replace(String str){
        return sensitiveWord.filterInfo(str);
    }


}
