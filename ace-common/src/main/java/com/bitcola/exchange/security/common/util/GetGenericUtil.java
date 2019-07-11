package com.bitcola.exchange.security.common.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GetGenericUtil<T> {
    public Class getMyClass() {
        System.out.println(this.getClass());
        Type type = getClass().getGenericSuperclass(); // 判断 是否泛型
        if (type instanceof ParameterizedType) {
            Type[] ptype = ((ParameterizedType) type).getActualTypeArguments();
            return (Class) ptype[0];
        } else {
            return Object.class;
        }
    }

    public static void main(String []s ){


        List<String> test=new ArrayList<>();


       var t=  new GetGenericUtil<List>().getMyClass();
       t.getSuperclass();


    }
}
