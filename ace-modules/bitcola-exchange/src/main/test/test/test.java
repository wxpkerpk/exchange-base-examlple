package test;
import com.bitcola.exchange.BitColaExchangeApplication;
import com.bitcola.exchange.entity.BatchBalance;
import com.bitcola.exchange.mapper.BalanceMapper;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.feign.IDataServiceFeign;
import com.bitcola.exchange.mapper.OrderMapper;
import com.bitcola.exchange.rest.ColaExchangeController;
import com.bitcola.exchange.script.ColaScriptController;
import com.bitcola.exchange.script.params.AutoMakeOrderParams;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.util.InFluxDbService;
import com.bitcola.exchange.websocket.WebSocketService;
import com.bitcola.me.entity.ColaUserEntity;
import org.hibernate.validator.constraints.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;


/*
 * @author:wx
 * @description:
 * @create:2018-08-30  22:00
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BitColaExchangeApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class test {

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Autowired
    BalanceMapper balanceMapper;
    @Autowired
    OrderMapper mapper;

    @Autowired
    ColaExchangeController colaExchangeController;

    @Autowired
    InFluxDbService inFluxDbService;

    @Autowired
    WebSocketService webSocketService;

    @Autowired
    ColaScriptController scriptController;


    @Test
    public void test1() throws Exception{
        //AppResponse eos_usdt = colaExchangeController.kline(null, 1551425745441L, 1551785745441L, 400, "15m", "EOS_USDT");
        //System.out.println(eos_usdt);

        webSocketService.sendFirstKline("kline_EOS_USDT_1h",null);

    }
    @Test
    public void test2() throws Exception{
        BaseContextHandler.setUserID(UserConstant.SYS_ADMIN);

        AutoMakeOrderParams params = new AutoMakeOrderParams();
        params.setPair("ZKS_EOS");
        params.setPerHourTime(1);
        params.setMinNumber(new BigDecimal(100));
        params.setMaxNumber(new BigDecimal(200));
        List<AutoMakeOrderParams> list = new ArrayList<>();
        list.add(params);
        scriptController.autoMakeOrder(list);

        Thread.sleep(100000);
    }



    @Test
    public void test3(){
        BaseContextHandler.setUserID(UserConstant.SYS_ADMIN);
        scriptController.getBalance();
    }



}
