package com.bitcola.exchange;

import com.bitcola.exchange.caculate.CaculateBootstrap;
import com.bitcola.exchange.caculate.config.WebSocket;
import com.bitcola.exchange.caculate.dataservice.ColaBalanceService;
import com.bitcola.exchange.caculate.dataservice.ColaMeService;
import com.bitcola.exchange.caculate.dataservice.ExchangeLogService;
import com.bitcola.exchange.caculate.dataservice.PushService;
import com.bitcola.exchange.caculate.kafka.KafkaSender;
import com.bitcola.exchange.caculate.message.PushMessage;
import com.bitcola.exchange.caculate.rest.ExchangeController;
import com.bitcola.exchange.caculate.rest.UserBalanceController;
import com.bitcola.exchange.caculate.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CaculateBootstrap.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestInfluxDb {



    @Autowired
    InFluxDbService inFluxDbService;
    @Autowired
    ColaMeService colaMeService;
    @Autowired
    ColaBalanceService colaBalanceService;
    @Autowired
    ExchangeService exchangeService;

    @Autowired
    ExchangeLogService exchangeLogService;
    @Autowired
    ExchangeUtils exchangeUtils;
    @Autowired
    ExchangeController exchangeController;
    @Autowired
    MatchService matchService;
    @Autowired
    UserBalanceController userBalanceController;
    @Autowired
    PushService pushService;
    @Autowired
    WebSocket webSocket;
    @Autowired
    KafkaSender kafkaSender;


    @Test
   public void inluxdbTest() throws InterruptedException {




        var pair="EOS_USDT";
        var userId="124";
        exchangeService.makeOrder("124",pair,BigDecimal.valueOf(10),BigDecimal.valueOf(4),BigDecimal.valueOf(40),"buy");
        exchangeService.makeOrder("124",pair,BigDecimal.valueOf(1),BigDecimal.valueOf(1), BigDecimal.valueOf(0),"sell");
        exchangeService.makeOrder("124",pair,BigDecimal.valueOf(2),BigDecimal.valueOf(1),BigDecimal.valueOf(0),"sell");
        exchangeService.makeOrder("124",pair,BigDecimal.valueOf(3),BigDecimal.valueOf(3),BigDecimal.valueOf(0),"sell");
        while(true){

            Thread.sleep(3000);


        }





//        exchangeService.makeOrder("100013","BTC_ETH",5000,1,5000,"sell");
//        exchangeService.makeOrder("100013","BTC_ETH",6000,2,6000*2,"buy");
//        exchangeService.caculateOrder("BTC_ETH");

//        exchangeService.makeOrder("100013","BTC_EOS",4000,1,4000,"sell");
//        exchangeService.makeOrder("100013","BTC_EOS",6000,3,6000*3,"buy");
//
  //    exchangeService.caculateOrder("ETC_EOS");
//
////
////       matchService.runUpdate("BTC_EOS");
//
////        var r1=colaMeService.getDepth("GAME_EOS",10,0.001,0);
//
//        var r2=exchangeUtils.getDepth("BTC_EOS",10,0.001,0);

//
//        matchService.runMatch=false;
//        exchangeService.makeOrder("100013","BTC_EOS",6985,0.2,6985*0.2,"buy");
//        exchangeService.makeOrder("100013","BTC_EOS",6985,0.2,0,"sell");
//        exchangeService.runCaculatorV2("AMP_EOS");
//
//        var s2=pushService.eosPrice();
//       var sss=  userBalanceController.getEOSAssessment("XLM_EOS");
//       sss.getMessage();
//

//        exchangeService.caculateOrder("BTC_USDT");

//        exchangeService.afterPropertiesSet();


//
//        MakeOrderParams makeOrderParams=new MakeOrderParams();
//        makeOrderParams.setCode("EOS_USDT");
//        makeOrderParams.setCount(BigDecimal.valueOf(1000));
//        makeOrderParams.setPrice(BigDecimal.valueOf(24.12));
//        makeOrderParams.setType("sell");
//        makeOrderParams.setSign("07ad9307fb433a441075bf2fb3298468");
//        makeOrderParams.setTime(1541052593402L);
//        String test= ExchangeController.makeSign(makeOrderParams,"457a3fcd-b1f6-488e-9444-a62fe685e621");
//
//
//
//        var result=exchangeUtils.getDepth("BTC_USDT", 10,0.01);
//
//        exchangeUtils.updateDepth("BTC_USDT");














    }



}
