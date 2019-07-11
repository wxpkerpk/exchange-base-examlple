package com.bitcola.chain.init;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.ChainKafkaServerApplication;
import com.bitcola.chain.cache.ChainCache;
import com.bitcola.chain.config.SpringContextsUtil;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.constant.ModuleStatusConstant;
import com.bitcola.chain.controller.ChainSendMessage;
import com.bitcola.chain.entity.ColaChainModule;
import com.bitcola.chain.entity.ColaChainWithdraw;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.mapper.ColaChainModuleMapper;
import com.bitcola.chain.mapper.ColaChainWithdrawMapper;
import com.bitcola.chain.server.BaseChainServer;
import com.bitcola.chain.util.NetUtil;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainOrder;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务运行和监控
 * @author zkq
 * @create 2019-01-21 12:43
 **/
@Log4j2
@Component
public class GlobalChain implements ApplicationRunner, Ordered {

    @Value("${server.port}")
    public String port;

    @Autowired
    ColaChainModuleMapper colaChainModuleMapper;
    @Autowired
    ColaChainDepositMapper colaChainDepositMapper;

    @Autowired
    ColaChainWithdrawMapper withdrawMapper;

    public static final Map<String, BaseChainServer> serverMap = new HashMap<>();

    public static String JAR_PATH;

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JAR_PATH = getJarPath();
        Map<String, BaseChainServer> map = SpringContextsUtil.applicationContext.getBeansOfType(BaseChainServer.class);
        for (String module : map.keySet()) {
            BaseChainServer chainServer = map.get(module);
            serverMap.put(chainServer.getModuleName().toUpperCase(),chainServer);
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(()->startModule());
    }

    /**
     * 启动
     */
    private void startModule(){
        // 获取 其中一个未启动的服务
        ColaChainModule module  = colaChainModuleMapper.getNotRunningModule();
        if (module != null){
            // 启动它
            module.setTimestamp(System.currentTimeMillis());
            module.setStatus(ModuleStatusConstant.PRE_RUNNING);
            module.setHost(getHost());
            colaChainModuleMapper.updateByPrimaryKeySelective(module);
            BaseChainServer chainServer = serverMap.get(module.getModule());
            if (chainServer!=null){
                chainServer.start();
                ChainCache.modules.add(module.getModule());
                log.info(module.getModule()+" 正在启动中.......");
                unRecord(module.getModule());
            }
        }

    }

    /**
     * 监控 server 运行情况
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void monitor(){
        for (String module : serverMap.keySet()) {
            BaseChainServer chainServer = serverMap.get(module);
            boolean status = chainServer.getStatus();
            if (status){
                ColaChainModule colaChainModule = colaChainModuleMapper.selectByPrimaryKey(module);
                colaChainModule.setTimestamp(System.currentTimeMillis());
                colaChainModule.setStatus(ModuleStatusConstant.RUNNING);
                colaChainModule.setHost(getHost());
                colaChainModuleMapper.updateByPrimaryKeySelective(colaChainModule);
            } else {
                ChainCache.modules.remove(chainServer.getModuleName());
            }
        }
        startModule();
    }

    /**
     * 推送未被处理的充值
     * @param module
     */
    public void unRecord(String module){
        ChainSendMessage sendMessage = SpringContextsUtil.applicationContext.getBean(ChainSendMessage.class);
        List<ColaChainDepositResponse> list = colaChainDepositMapper.unRecord(module);
        for (ColaChainDepositResponse colaChainDepositResponse : list) {
            BaseChainServer chainServer = serverMap.get(colaChainDepositResponse.getModule());
            String orderId = sendMessage.deposit(colaChainDepositResponse);
            if (orderId != null && chainServer!=null){
                colaChainDepositResponse.setOrderId(orderId);
                colaChainDepositMapper.updateByPrimaryKeySelective(colaChainDepositResponse);
                chainServer.unConfirm.put(colaChainDepositResponse.getHash(),0);
            }
        }
    }


    /**
     * 处理提币
     * 1 获取属于当前服务的订单
     * 2 循环处理订单,并发送处理结果
     *
     */
    @Scheduled(cron = "30 0/1 * * * ?")
    public void dealExportedWithdraw() throws Throwable{
        ChainSendMessage sendMessage = SpringContextsUtil.applicationContext.getBean(ChainSendMessage.class);
        Set<String> modules = ChainCache.modules;
        for (String module : modules) {
            List<JSONObject> exportedOrder = sendMessage.getExportedOrder(module);
            for (JSONObject jsonObject : exportedOrder) {
                ColaChainOrder chainOrder = jsonObject.toJavaObject(ColaChainOrder.class);
                BaseChainServer chainServer = serverMap.get(module);
                if (chainServer != null){
                    ColaChainWithdraw order = withdrawMapper.selectByPrimaryKey(chainOrder.getOrderId());
                    if (order==null){
                        order = new ColaChainWithdraw();
                        order.setOrderId(chainOrder.getOrderId());
                        order.setModule(module);
                        order.setCoinCode(chainOrder.getCoinCode());
                        order.setAddress(chainOrder.getAddress());
                        order.setNumber(chainOrder.getNumber());
                        order.setMemo(chainOrder.getMemo());
                        order.setStatus(DepositStatusConstant.NOT_CONFIRM);
                        withdrawMapper.insertSelective(order);
                    } else {
                        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
                        String status = order.getStatus();
                        if (DepositStatusConstant.NOT_CONFIRM.equals(status)){
                            response.setSuccess(false);
                            response.setErrMessage(order.getError());
                        } else {
                            response.setSuccess(true);
                            response.setHash(order.getHash());
                            response.setFee(order.getFee());
                            response.setFeeCoinCode(order.getFeeCoinCode());
                        }
                        response.setOrderId(order.getOrderId());
                        sendMessage.dealWithdraw(response);
                        return;
                    }
                    log.info("=====================  提币日志  ======================");
                    log.info(" 订单id: "+chainOrder.getOrderId());
                    log.info(" 币种为: "+chainOrder.getCoinCode());
                    log.info(" 转出地址: "+chainOrder.getAddress());
                    log.info(" 转出数量: "+chainOrder.getNumber());
                    log.info(" memo: "+chainOrder.getMemo());
                    ColaChainWithdrawResponse withdraw = chainServer.withdraw(chainOrder.getCoinCode(), chainOrder.getAddress(), chainOrder.getNumber(), chainOrder.getMemo());
                    if (withdraw.isSuccess()){
                        log.info(" ...");
                        log.info(" hash: "+withdraw.getHash());
                        log.info("=====================  提币成功  ======================");
                        order.setStatus(DepositStatusConstant.CONFIRM);
                        order.setHash(withdraw.getHash());
                        order.setFee(withdraw.getFee());
                        order.setFeeCoinCode(withdraw.getFeeCoinCode());
                        withdrawMapper.updateByPrimaryKeySelective(order);
                    } else {
                        log.info("");
                        log.info("失败原因: "+withdraw.getErrMessage());
                        log.info("===================  提币失败  ====================");
                        order.setStatus(DepositStatusConstant.NOT_CONFIRM);
                        order.setError(withdraw.getErrMessage());
                        withdrawMapper.updateByPrimaryKeySelective(order);
                    }
                    withdraw.setOrderId(order.getOrderId());
                    sendMessage.dealWithdraw(withdraw);
                    log.info("===================  系统以已经记录  ====================");
                }
            }
        }
    }


    private String getHost(){
        return NetUtil.getLocalIpAddress();
    }

    private static String getJarPath(){
        ApplicationHome home = new ApplicationHome(ChainKafkaServerApplication.class);
        File jarFile = home.getSource();
        return jarFile.getParent();
    }

}
