package com.bitcola.exchange.security.common.msg;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-05-05 18:47
 **/
@Data
public class AppPageResponse<T> extends AppResponse<T> {

    /**
     * 光标,用于分页,每次分页传入上一次返回的此参数,第一次请求不传入此参数(或者 null )
     *      当此字段返回 null 表示没有下一页数据了
     */
    Long cursor;

    public AppPageResponse(){
        super();
    }


}
