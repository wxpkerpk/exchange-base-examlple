package com.bitcola.exchange.launchpad.service;

import com.bitcola.exchange.launchpad.biz.ColaResonanceBiz;
import com.bitcola.exchange.launchpad.config.SpringBeanHandler;
import com.bitcola.exchange.launchpad.project.ResonanceProject;
import com.bitcola.exchange.launchpad.vo.ResonanceBuyMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * @author zkq
 * @create 2019-05-12 10:21
 **/
@Service
public class ColaResonanceBuyService implements ApplicationRunner {

    @Autowired
    BlockingQueue<ResonanceBuyMessage> buyQueue;

    @Autowired
    ColaResonanceBiz biz;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, ResonanceProject> map = SpringBeanHandler.applicationContext.getBeansOfType(ResonanceProject.class);
        for (String module : map.keySet()) {
            ResonanceProject project = map.get(module);
            ColaResonanceBiz.projects.put(project.coinCode().toUpperCase(),project);
        }
        while (true){
            try {
                ResonanceBuyMessage message = buyQueue.take();
                dealMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dealMessage(ResonanceBuyMessage message) {
        biz.buy(message);
    }
}
