package com.bitcola.exchange.bitcolapush.controller;

import com.bitcola.exchange.bitcolapush.http.PriceUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-11-14 10:09
 **/
@RequestMapping("price")
@RestController
public class PriceController {

    @RequestMapping("eos")
    public BigDecimal eos(){
        return PriceUtil.getEosPrice();
    }

    @RequestMapping("eth")
    public BigDecimal eth(){
        return PriceUtil.getEthPrice();
    }

    @RequestMapping("btc")
    public BigDecimal btc(){
        return PriceUtil.getBtcPrice();
    }



}
