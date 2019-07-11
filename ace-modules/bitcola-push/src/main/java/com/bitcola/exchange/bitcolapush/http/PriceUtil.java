package com.bitcola.exchange.bitcolapush.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author zkq
 * @create 2018-10-21 20:21
 **/
@Component
public class PriceUtil {


    private static BigDecimal eosPrice = BigDecimal.ZERO;
    private static BigDecimal ethPrice = BigDecimal.ZERO;
    private static BigDecimal btcPrice = BigDecimal.ZERO;

    public static BigDecimal getBtcPrice(){
        return btcPrice;
    }

    public static BigDecimal getEosPrice(){
        return eosPrice;
    }

    public static BigDecimal getEthPrice(){
        return ethPrice;
    }


    @Scheduled(cron = "0 */1 * * * ?")
    public void refreshEthPrice(){
        btcPrice = refreshPrice("btc");
        eosPrice = refreshPrice("eos");
        ethPrice = refreshPrice("eth");
    }


    private BigDecimal refreshPrice(String coin){
        BigDecimal zb = BigDecimal.ZERO;
        try {
            zb = new BigDecimal(getPriceByZB(coin));
        } catch (Exception e){
        }
        BigDecimal hb = zb;
        try {
        //    hb = new BigDecimal(getPriceByHuobi(coin));
        } catch (Exception e){
        }
        BigDecimal ba = hb;
        try {
        //    ba = new BigDecimal(getPriceByBinan(coin));
        } catch (Exception e){
        }
        return zb.add(hb).add(ba).divide(new BigDecimal(3),2, RoundingMode.HALF_UP);
    }



    /**
     * eth zb的价格
     * @return
     */
    private static String getPriceByZB(String coin) throws IOException, HttpException {
        String result= OKHttpUtil.httpGet("http://api.zb.cn/data/v1/ticker?"+("market="+coin.toLowerCase()+"_usdt"));
        return JSONObject.parseObject(result).getJSONObject("ticker").getString("last");
    }

    public static void main(String[] args)throws Exception {
        String eos = getPriceByHuobi("eth");
        System.out.println(eos);
    }

    /**
     * eth okex的价格
     * @return
     * @throws HttpException
     * @throws IOException
     */
    private static String getPriceByOkex(String coin) throws HttpException, IOException {
        String param = coin.toUpperCase()+"_USD";
        String result= OKHttpUtil.httpGet("https://www.okcoin.com/api/spot/v3/instruments/"+param+"/ticker");

        return JSONObject.parseObject(result).getString("last");
    }
    /**
     * 币安 的价格
     * @return
     * @throws HttpException
     * @throws IOException
     */
    private static String getPriceByBinan(String coin) throws HttpException, IOException {

        String s1= OKHttpUtil.httpGet("https://api.binance.com/api/v3/ticker/price?symbol="+coin.toUpperCase()+"USDT");
        return JSONObject.parseObject(s1).getString("price");
    }
    /**
     * 火币 的价格
     * @return
     * @throws HttpException
     * @throws IOException
     */
    private static String getPriceByHuobi(String coin) throws HttpException, IOException {
        String s= OKHttpUtil.httpGet("https://api.huobipro.com/market/trade?symbol="+coin.toLowerCase()+"usdt");

        return JSONObject.parseObject(s).getJSONObject("tick").getJSONArray("data").getJSONObject(0).getString("price");
    }

}
