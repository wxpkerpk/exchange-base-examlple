package com.bitcola.exchange.bitcolapush.controller;

import com.bitcola.exchange.bitcolapush.http.ExchangeRate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-13 17:24
 **/
@RestController
@RequestMapping("currency")
public class WordCurrencyPriceController {

    @RequestMapping("rate")
    public Map<String,BigDecimal> currency(){
        if ((ExchangeRate.JPY.compareTo(BigDecimal.ZERO)==0 || ExchangeRate.CNY.compareTo(BigDecimal.ZERO)==0 ||
                ExchangeRate.GBP.compareTo(BigDecimal.ZERO)==0 || ExchangeRate.EUR.compareTo(BigDecimal.ZERO)==0)){
            ExchangeRate.refreshRate();
        }
        Map<String,BigDecimal> result = new LinkedHashMap<>();
        result.put("CNY",ExchangeRate.CNY);
        result.put("USD",BigDecimal.ONE);
        result.put("EUR",ExchangeRate.EUR);
        result.put("JPY",ExchangeRate.JPY);
        result.put("GBP",ExchangeRate.GBP);
        return result;
    }

}
