package com.bitcola.caculate.entity.Vo;

import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.caculate.entity.ExchangeLog;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*
 * @author:wx
 * @description:
 * @create:2018-10-06  18:29
 */
@Data
public class VoCaculateParams {
    List<String>completed;
    ColaOrder unCompleted;
    List<TransForms>transForms;
    List<Payback>paybacks;
    List<ExchangeLog> exchangeLogs;

    public VoCaculateParams(List<String> completed, ColaOrder unCompleted, List<TransForms> transForms, List<Payback> paybacks, List<ExchangeLog> exchangeLogs) {
        this.completed = completed;
        this.unCompleted = unCompleted;
        this.transForms = transForms;
        this.paybacks = paybacks;
        this.exchangeLogs = exchangeLogs;
    }
}
