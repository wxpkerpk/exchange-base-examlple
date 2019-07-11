package test;

import com.bitcola.activity.BitColaActivityApplication;
import com.bitcola.activity.biz.InnerTestBiz;
import com.bitcola.activity.entity.InnerTest;
import com.bitcola.activity.entity.SignUp;
import com.bitcola.activity.feign.IDataServiceFeign;
import com.bitcola.activity.mapper.InnerTestMapper;
import com.bitcola.activity.mapper.SignUpMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.me.entity.ColaUserEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.UUID;


/*
 * @author:wx
 * @description:
 * @create:2018-08-30  22:00
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BitColaActivityApplication.class)
public class test {

    @Autowired
    InnerTestMapper mapper;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    InnerTestBiz testBiz;

    @Test
    public void test1() throws Exception{
        ColaUserEntity userEntity = dataServiceFeign.infoByInviterCode("123");
        if (userEntity == null){
            System.out.println("ok");
        }
    }

    @Test
    public void test2(){
        InnerTest test = new InnerTest();
        test.setId(UUID.randomUUID().toString());
        test.setUserId("100044");
        test.setNumber(new BigDecimal(200));
        test.setTimestamp(System.currentTimeMillis());
        test.setOrderId(UUID.randomUUID().toString());
        mapper.insertSelective(test);
    }

}
