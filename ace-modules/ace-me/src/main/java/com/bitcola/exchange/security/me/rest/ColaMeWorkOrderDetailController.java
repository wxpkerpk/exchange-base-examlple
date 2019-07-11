package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.exchange.security.me.biz.ColaMeWorkOrderDetailBiz;
import com.bitcola.me.entity.ColaMeWorkOrderDetail;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("colaMeWorkOrderDetail")
public class ColaMeWorkOrderDetailController extends BaseController<ColaMeWorkOrderDetailBiz, ColaMeWorkOrderDetail> {

}