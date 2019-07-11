package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaSmsBiz;
import com.bitcola.exchange.security.admin.entity.ColaSms;
import com.bitcola.exchange.security.common.constant.SmsModuleConstant;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信管理
 * @author zkq
 * @create 2019-05-09 14:43
 **/
@RestController
@RequestMapping("cola/sms")
public class ColaSmsManagerController extends BaseController<ColaSmsBiz, ColaSms> {
    /**
     * 后缀,SmsModuleConstant.class 这个常量类的后缀是这个作为 value
     */
    public static final String SUFFIX = "_MSG";

    @RequestMapping("getSmsModule")
    public AppResponse getSmsModule() throws Exception{
        List<Map<String,Object>> list = new ArrayList<>();
        Field[] fields = SmsModuleConstant.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (name.endsWith(SUFFIX)){
                Map<String,Object> map = new HashMap<>();
                map.put("key",name.replace(SUFFIX,""));
                map.put("value",field.get(name));
                list.add(map);
            }
        }
        return AppResponse.ok().data(list);
    }






}
