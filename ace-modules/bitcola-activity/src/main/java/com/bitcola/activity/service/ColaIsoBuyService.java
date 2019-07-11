package com.bitcola.activity.service;

import com.bitcola.activity.biz.ColaIsoBiz;
import com.bitcola.activity.msg.BuyMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

/**
 * @author zkq
 * @create 2019-05-12 10:21
 **/
@Service
public class ColaIsoBuyService implements ApplicationRunner {

    @Autowired
    BlockingQueue<BuyMessage> buyQueue;

    @Autowired
    ColaIsoBiz biz;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        while (true){
            try {
                BuyMessage message = buyQueue.take();
                dealMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dealMessage(BuyMessage message) {
        biz.buy(message);
    }
}
