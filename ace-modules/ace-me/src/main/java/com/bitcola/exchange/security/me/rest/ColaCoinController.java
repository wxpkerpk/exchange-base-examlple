package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaCoinBiz;
import com.bitcola.me.entity.ColaCoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("colaCoin")
public class ColaCoinController {


    @Autowired
    ColaCoinBiz biz;

    /**
     * 通过 coinCode 获取币种信息
     * @param coinCode
     * @return
     */
    @RequestMapping("getByCoinCode")
    public AppResponse getByCoinCode(String coinCode) {
        ColaCoin coin =  biz.getByCoinCode(coinCode);
        AppResponse resp = new AppResponse<>();
        resp.setData(coin);
        return resp;
    }

    @IgnoreUserToken
    @RequestMapping("list")
    public AppResponse list(){
        List<Map<String,String>> list = biz.list();
        return AppResponse.ok().data(list);
    }





    /**
     * 添加新币种
     * @return
     */
    @RequestMapping(value = "addCoin",method = RequestMethod.POST)
    public AppResponse addCoin(@RequestBody ColaCoin coin) {
        coin.setId(UUID.randomUUID().toString());
        biz.addCoin(coin);
        return new AppResponse();
    }
    /**
     * 更新币种
     * @return
     */
    @RequestMapping(value = "updateCoin",method = RequestMethod.POST)
    public AppResponse updateCoin(@RequestBody ColaCoin coin) {
        biz.updateById(coin);
        return new AppResponse();
    }


    /**
     * 获取 USDT 的价格
     * @param coin
     * @return
     */
    @RequestMapping(value = "getUsdtPrice",method = RequestMethod.GET)
    public AppResponse getUsdtPrice(String coin){
        BigDecimal price = biz.getCoinWorth(coin);
        return AppResponse.ok().data(price);
    }

    @RequestMapping(value = "getCoinWorth",method = RequestMethod.GET)
    public AppResponse getCoinWorth(String coin){
        BigDecimal price = biz.getCoinWorth(coin);
        return AppResponse.ok().data(price);
    }

    @RequestMapping(value = "allCoinWorth",method = RequestMethod.GET)
    public AppResponse allCoinWorth(){
        Map<String,BigDecimal> map = biz.allCoinWorth();
        return AppResponse.ok().data(map);
    }
}