package test;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.launchpad.BitColaExchangeLaunchpadApplication;
import com.bitcola.exchange.launchpad.biz.ColaLaunchpadExchangeLogBiz;
import com.bitcola.exchange.launchpad.biz.ColaLaunchpadProjectBiz;
import com.bitcola.exchange.launchpad.dto.ColaLaunchpadProjectList;
import com.bitcola.exchange.launchpad.rest.ColaLaunchpadApplyController;
import com.bitcola.exchange.launchpad.rest.ColaLaunchpadExchangeLogController;
import com.bitcola.exchange.launchpad.rest.ColaLaunchpadProjectController;
import com.bitcola.exchange.launchpad.vo.BuyParams;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
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
@SpringBootTest(classes = BitColaExchangeLaunchpadApplication.class)
public class test {

    @Autowired
    ColaLaunchpadApplyController controller;

    @Autowired
    ColaLaunchpadProjectController projectController;

    @Autowired
    ColaLaunchpadExchangeLogController exchangeLogController;

    @Autowired
    ColaLaunchpadExchangeLogBiz biz;

    @Test
    public void test1() throws Exception{
        BaseContextHandler.setUserID("101");
        BuyParams params = new BuyParams();
        params.setId("104f94df-75f2-40eb-8446-a5d1ba9e5a3c");
        params.setPin("123456");
        params.setNumber(new BigDecimal(2));
        params.setSymbol("USDT");
        params.setTicket("1");
        params.setRand("1");
        params.setUserIp("1");
        projectController.buy(params,null);
    }

    @Test
    public void test2(){
        BaseContextHandler.setUserID("101");
        //map.put("page",1);
        //map.put("page",1);
        Map<String, Object> issue1 = biz.issue("f4985775-3223-4fc3-a1ad-3d437d3f51fd", true, new BigDecimal("0.2"));
        JSONObject.toJSONString(issue1);
    }




}
