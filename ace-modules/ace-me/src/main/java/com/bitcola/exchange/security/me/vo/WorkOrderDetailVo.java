package com.bitcola.exchange.security.me.vo;


import com.bitcola.me.entity.ColaMeWorkOrder;
import com.bitcola.me.entity.ColaMeWorkOrderDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * 工单
 *
 * @author zkq
 * @create 2018-09-13 10:50
 **/
public class WorkOrderDetailVo {

    private ColaMeWorkOrder workOrder;

    private List<ColaMeWorkOrderDetail> detailList;

    public ColaMeWorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(ColaMeWorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public List<ColaMeWorkOrderDetail> getDetailList() {
        if (detailList == null){
            detailList = new ArrayList<>();
        }
        return detailList;
    }

    public void setDetailList(List<ColaMeWorkOrderDetail> detailList) {
        this.detailList = detailList;
    }
}
