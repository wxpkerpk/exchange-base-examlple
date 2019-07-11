package com.bitcola.chain.rest;

import com.bitcola.chain.server.eth.EthChainServer;
import com.bitcola.exchange.security.common.msg.AppResponse;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zkq
 * @create 2019-04-12 12:24
 **/
@RestController
@RequestMapping("chain")
@Log4j2
public class ChainController {

    ExecutorService executorService = Executors.newFixedThreadPool(20);
    @Autowired
    EthChainServer ethChainServer;

    @RequestMapping("ethScan")
    public AppResponse ethScan(long start,long end) {
        for (long i = start; i <= end ; i++) {
            try {
                log.info("手动扫描区块: "+i);
                ethChainServer.scanBlock(i,executorService);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AppResponse.ok();
    }




}
