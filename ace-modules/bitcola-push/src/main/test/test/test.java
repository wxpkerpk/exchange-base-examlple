package test;


import com.bitcola.exchange.bitcolapush.BitColaPushApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * @author:wx
 * @description:
 * @create:2018-08-30  22:00
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BitColaPushApplication.class)
public class test {


    @Test
    public void test1(){

        System.out.println(1);
    }

}
