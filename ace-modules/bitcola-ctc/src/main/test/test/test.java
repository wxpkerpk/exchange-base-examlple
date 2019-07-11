package test;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.ctc.BitColaExchangeCtcApplication;
import com.bitcola.exchange.ctc.biz.ColaCtcExchangeLogBiz;
import com.bitcola.exchange.ctc.biz.ColaCtcProjectBiz;
import com.bitcola.exchange.ctc.dto.ColaCtcProjectList;
import com.bitcola.exchange.ctc.rest.ColaCtcApplyController;
import com.bitcola.exchange.ctc.rest.ColaCtcExchangeLogController;
import com.bitcola.exchange.ctc.rest.ColaCtcProjectController;
import com.bitcola.exchange.ctc.vo.BuyParams;
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
@SpringBootTest(classes = BitColaExchangeCtcApplication.class)
public class test {


    @Test
    public void test1() throws Exception{
    }

    @Test
    public void test2(){
    }




}
