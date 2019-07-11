package com.bitcola.exchange.security.admin.rest;


import com.bitcola.exchange.security.admin.biz.ColaCoinSymbolBiz;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.me.entity.ColaCoinSymbol;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("cola/coinSymbol")
public class ColaCoinSymbolController extends BaseController<ColaCoinSymbolBiz, ColaCoinSymbol> {


}