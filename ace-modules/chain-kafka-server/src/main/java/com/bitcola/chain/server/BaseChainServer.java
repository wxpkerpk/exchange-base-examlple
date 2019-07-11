package com.bitcola.chain.server;

import com.alibaba.fastjson.JSONObject;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.chain.chain.newton.NewTonCore;
import com.bitcola.chain.chain.newton.entity.NewTonTransaction;
import com.bitcola.chain.config.SpringContextsUtil;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.controller.ChainSendMessage;
import com.bitcola.chain.entity.ColaChainCoin;
import com.bitcola.chain.entity.ColaChainEthKey;
import com.bitcola.chain.mapper.ColaChainCoinMapper;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.util.AESUtil;
import com.bitcola.chain.util.MemoUtil;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.activation.CommandInfo;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务的父类,所有服务继承父类
 */
@Log4j2
public abstract class BaseChainServer {

    /**
     * 当前服务运行状态
     */
    public boolean running = false;
    /**
     * 未确认的充值
     */
    public Map<String,Integer> unConfirm = new HashMap<>();
    /**
     * 扫描这些地址
     */
    public Set<String> address = new HashSet<>();

    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    protected ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void start(){
        cachedThreadPool.submit(()->{
            try {
                running = true;
                run();
            } catch (Throwable e){
                running = false;
                log.error(e.getMessage(),e);
            }
        });
    }

    public static final AtomicLong count = new AtomicLong(0);

    /**
     * 当前服务名字
     */
    public abstract String getModuleName();

    /**
     * 当前服务运行状态
     */
    public boolean getStatus(){
        return running;
    }
    /**
     * 服务运行执行的方法
     */
    protected abstract void run() throws Throwable;

    /**
     * 新账户
     * @param coinCode 币种,可能是代币
     * @return
     */
    public abstract String newAccount(String coinCode) throws Throwable;

    /**
     * 检测地址是否正确
     */
    public abstract boolean checkAddress(String address);

    /**
     *  提币
     * @param coinCode 币种,可能是代币
     * @param address 提币地址
     * @param number 提币数量
     * @param memo 备注 (可以不使用)
     * @return
     */
    public abstract ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable;

    /**
     * 确认数
     */
    public abstract void confirm() throws Throwable;

    /**
     * 推送充值
     * @param deposit
     * @return
     */
    public String deposit(ColaChainDepositResponse deposit){
        log.info(" =====================  充值日志  ======================");
        log.info(" 币种: "+deposit.getCoinCode());
        log.info(" 数量: "+deposit.getAmount());
        log.info(" memo: "+deposit.getMemo());
        log.info(" hash: "+deposit.getHash());
        log.info(" ...... ");
        ColaChainDepositMapper depositBiz = SpringContextsUtil.applicationContext.getBean(ColaChainDepositMapper.class);
        ChainSendMessage sendMessage = SpringContextsUtil.applicationContext.getBean(ChainSendMessage.class);
        depositBiz.insertSelective(deposit);
        String orderId = sendMessage.deposit(deposit);
        if (orderId != null && !"test".equalsIgnoreCase(orderId)){
            deposit.setOrderId(orderId);
            deposit.setStatus(DepositStatusConstant.NOT_CONFIRM);
            depositBiz.updateByPrimaryKeySelective(deposit);
            log.info("订单 ID: "+orderId);
            log.info("====================  充值已经入库  =====================");
            return orderId;
        } else {
            log.info("===================  非当前环境充值,或者为开放充值  ====================");
            return null;
        }
    }
    /**
     * 区块确认数量 +1
     * @return
     */
    protected Integer confirmNumber( Integer currentConfirmNumber,String orderId){
        ChainSendMessage bean = SpringContextsUtil.applicationContext.getBean(ChainSendMessage.class);
        return bean.confirmNumber(currentConfirmNumber,orderId);
    }
    /**
     * 完成充值
     * @return
     */
    protected boolean completeDeposit( String orderId){
        ChainSendMessage bean = SpringContextsUtil.applicationContext.getBean(ChainSendMessage.class);
        return bean.completeDeposit(orderId);
    }
    /**
     * 获得需要监控的地址
     * @return
     */
    protected List<String> getAddress(){
        ChainSendMessage bean = SpringContextsUtil.applicationContext.getBean(ChainSendMessage.class);
        return bean.getAddress(getModuleName());
    }

    public abstract ColaChainBalance getChainBalance(String coinCode, String feeCoinCode) throws Throwable;


    protected static final Map<String,ColaChainCoin> CHAIN_COIN_CACHE = new ConcurrentHashMap<>();


    protected ColaChainCoin getCoinCode(String coinCode){
        return CHAIN_COIN_CACHE.computeIfAbsent(coinCode,k ->
                SpringContextsUtil.applicationContext.getBean(ColaChainCoinMapper.class).selectByPrimaryKey(coinCode));
    }

    @Cached(expire = 10,timeUnit = TimeUnit.MINUTES,cacheType = CacheType.LOCAL)
    protected int getConfirmNumber(String coinCode){
        return getCoinCode(coinCode).getConfirmNumber();
    }

    @Cached(expire = 10,timeUnit = TimeUnit.MINUTES,cacheType = CacheType.LOCAL)
    protected BigDecimal getDepositMin(String coinCode){
        return getCoinCode(coinCode).getDepositMin();
    }

    protected ColaChainDepositResponse createDepositEntity(String hash,BigDecimal number,String conCode,String module,
                                                                  String status,String toAddress,String memo){
        ColaChainDepositResponse deposit = new ColaChainDepositResponse();
        deposit.setHash(hash);
        deposit.setAmount(number);
        deposit.setTimestamp(System.currentTimeMillis());
        deposit.setModule(module);
        deposit.setCoinCode(conCode);
        deposit.setStatus(status);
        deposit.setToAddress(toAddress);
        deposit.setMemo(memo);
        return deposit;
    }


}
