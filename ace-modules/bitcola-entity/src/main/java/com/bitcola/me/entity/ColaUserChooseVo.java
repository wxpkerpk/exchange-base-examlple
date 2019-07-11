package com.bitcola.me.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/*
 * @author:wx
 * @description:
 * @create:2018-09-24  17:19
 */
@Data
public class ColaUserChooseVo {
        private static final long serialVersionUID = 1L;



        //交易介质
        private String symbol;
        //币种
        private String coinCode;
        //图标

        String icon;
        Integer sort;

        public String getPair()
        {


                return coinCode+"_"+symbol;
        }


    }
