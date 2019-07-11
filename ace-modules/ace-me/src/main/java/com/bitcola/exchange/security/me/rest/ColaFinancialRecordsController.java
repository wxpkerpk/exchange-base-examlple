package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.CSVUtil;
import com.bitcola.exchange.security.me.biz.ColaFinancialRecordsBiz;
import com.bitcola.exchange.security.me.dto.FinancialRecordsDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 资金记录
 *
 * @author zkq
 * @create 2018-10-24 19:17
 **/
@RestController
@RequestMapping("financialRecord")
public class ColaFinancialRecordsController {

    @Autowired
    ColaFinancialRecordsBiz biz;


    @RequestMapping("list")
    public TableResultResponse list(FinancialRecordsDto dto){
        int excludeInviteRewards = dto.getExcludeInviteRewards();
        if (excludeInviteRewards!=1 && excludeInviteRewards !=0){
            return null;
        }
        if (dto.getLimit()>100){
            return null;
        }
        TableResultResponse result = biz.list(dto);
        return result;
    }

    @RequestMapping("actionList")
    public AppResponse actionList(){
        List<String> list = biz.actionList();
        return AppResponse.ok().data(list);
    }

    @RequestMapping("csv")
    public void csv(HttpServletResponse response,Integer excludeInviteRewards) throws IOException {
        if (excludeInviteRewards!=1 && excludeInviteRewards !=0){
            return;
        }
        String[] headers = {"Nº","Time","Assets","Action type","account","status"};
        if (ColaLanguage.getCurrentLanguage().equals(ColaLanguage.LANGUAGE_CN)){
            headers = new String[]{"Nº","时间","资产","资金出入方式","金额","状态"};
        }
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment;filename=record.csv");
        List<Object[]> list = biz.csv(excludeInviteRewards);
        CSVUtil.downloadCVS(response.getOutputStream(),headers,list);
    }


    @RequestMapping("detail")
    public AppResponse detail(String id,String action){
        if (StringUtils.isAnyBlank(id,action)){
            return AppResponse.paramsError();
        }
        Map<String,String> map = biz.detail(id,action);
        return AppResponse.ok().data(map);
    }


}
