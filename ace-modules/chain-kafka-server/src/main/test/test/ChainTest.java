package test;

import com.bitcola.chain.ChainKafkaServerApplication;
import com.bitcola.chain.chain.btc.BtcCore;
import com.bitcola.chain.chain.usdt.UsdtCore;
import com.bitcola.chain.server.btc.BtcChainServer;
import com.bitcola.chain.server.eth.EthChainServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @author zkq
 * @create 2019-01-10 17:54
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChainKafkaServerApplication.class)
public class ChainTest {
    //
    //@Autowired
    //UsdtCore usdtCore;
    //@Autowired
    //BtcCore btcCore;
    //
    //@Test
    //public void test1() throws Throwable {
    //    String transactions = usdtCore.getTransactions(100, 0);
    //    System.out.println(transactions);
    //}

    @Autowired
    EthChainServer chainServer;

    @Test
    public void test2() throws Throwable {
        chainServer.transfer();
    }


}
