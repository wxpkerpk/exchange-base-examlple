package com.bitcola.chain.server.tt;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.eth.EthCore;
import com.bitcola.chain.chain.tt.TTCore;
import com.bitcola.chain.chain.tt.TTTransaction;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.entity.*;
import com.bitcola.chain.mapper.*;
import com.bitcola.chain.server.BaseChainServer;
import com.bitcola.chain.util.AESUtil;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zkq
 * @create 2019-04-09 12:42
 **/
@Log4j2
@Component
public class TTChainServer extends BaseChainServer {

    // hash 扫描线程
    public static ExecutorService executor = Executors.newFixedThreadPool(40);
    // 扫描出错的区块和 hash
    public static final HashMap<Long,Integer> ERROR_BLOCK = new HashMap<>();
    public static final HashMap<String,Integer> ERROR_HASH = new HashMap<>();

    @Autowired
    ColaChainDepositMapper depositMapper;

    @Autowired
    ColaChainKeyMapper keyMapper;

    @Autowired
    ColaChainTTKeyMapper ttKeyMapper;

    @Autowired
    ColaChainTTTokenMapper ttTokenMapper;

    @Autowired
    ColaChainEthNonceMapper nonceMapper;

    @Value("${bitcola.chain.password}")
    String encoderKey;

    @Override
    protected void run() throws Throwable {
        address.addAll(getAddress());
        List<ColaChainDepositResponse> unConfirmDeposit = depositMapper.unConfirm(getModuleName());
        for (ColaChainDepositResponse depositResponse : unConfirmDeposit) {
            unConfirm.put(depositResponse.getHash(),0);
        }
        long currentScanNumber = ttKeyMapper.getStartBlockNumber();
        Web3j web3j = TTCore.getWeb3j();
        long latestBlockNumber = 0;
        try {
            latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (Exception e) {
            log.info("TT 初始化错误,没有获得最新区块");
            log.error(e.getMessage(),e);
            run();
        }
        while (true){
            try {
                if (latestBlockNumber!=0 && currentScanNumber >= latestBlockNumber){
                    // 当前扫描到了最新的区块,等待5秒继续扫描
                    Thread.sleep(5*1000);
                    try {
                        latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber().longValue();
                    } catch (IOException e) {
                        log.error(e.getMessage(),e);
                    }
                    continue;
                }
                if (currentScanNumber%100 == 0){
                    log.info("TT 当前扫描区块: "+currentScanNumber);
                }
                try {
                    scanBlock(currentScanNumber);
                } catch (Exception e) {
                    log.info("TT 扫描区块出错: "+currentScanNumber+" ,错误:"+e.getMessage());
                    ERROR_BLOCK.put(currentScanNumber,0);
                }
                executor.shutdown();
                while (!executor.isTerminated()) {
                    Thread.sleep(30);
                }
                executor = Executors.newFixedThreadPool(40);
                // 存下当前扫描到的区块
                currentScanNumber++;
                recordCurrentEthBlockNumber(currentScanNumber);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }
    }

    private void scanBlock(long currentScanNumber) throws Exception{
        Request<?, EthBlock> ethBlockRequest =
                TTCore.getWeb3j().ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(currentScanNumber)), false);
        EthBlock ethBlock = ethBlockRequest.sendAsync().get();
        List<EthBlock.TransactionResult> transactions = ethBlock.getBlock().getTransactions();
        for (EthBlock.TransactionResult transaction : transactions) {
            String hash = transaction.get().toString();
            executor.submit(()->{
                try {
                    scanTransaction(hash);
                } catch (Exception e) {
                    log.info("TT 扫描hash出错: "+hash+" ,错误:"+e.getMessage());
                    ERROR_HASH.put(hash,0);
                }
            });
        }
    }

    private void scanTransaction(String hash) throws Exception{
        TTTransaction transaction = TTCore.getTransactionByHash(hash);
        if (address.contains(transaction.getTo()) && transaction.isStatus()){
            BigDecimal number = transaction.getTTNumber();
            if (getDepositMin(getModuleName()).compareTo(number)<=0){
                ColaChainDepositResponse depositEntity = createDepositEntity(hash, number, getModuleName(), getModuleName(), DepositStatusConstant.NOT_RECORD, transaction.getTo(), "");
                String orderId = deposit(depositEntity);
                if (StringUtils.isNotBlank(orderId)){
                    unConfirm.put(hash,0);
                    cachedThreadPool.submit(()->{
                        transferCoin(getModuleName(),getPrivateKey(depositEntity.getToAddress()),depositEntity.getToAddress());
                    });
                }
            }
        } else {
            // todo token ,新币,暂时不做
        }

    }


    private String getPrivateKey(String address){
        ColaChainTTKey ethKey = ttKeyMapper.selectByPrimaryKey(address);
        return AESUtil.decrypt(ethKey.getPrivateKey(),encoderKey);
    }

    private void transferCoin(String coinCode,String privateKey,String address){
        try {
            BigDecimal balance = TTCore.getEthDecimal(TTCore.getWeb3j().ethGetBalance(address,
                    DefaultBlockParameterName.LATEST).send().getBalance());
            ColaChainCoin coin = getCoinCode(coinCode);
            if ("TT".equals(coinCode)){
                // 查询资金剩余情况
                if (balance.compareTo(coin.getTransferToHotLimit())>0){
                    EthGasPrice ethGasPrice = TTCore.getWeb3j().ethGasPrice().sendAsync().get();
                    BigInteger GAS_PRICE = ethGasPrice.getGasPrice();
                    BigInteger GAS_LIMIT = BigInteger.valueOf(21_000L);
                    BigInteger realNumber = TTCore.getEthInteger(balance).subtract(GAS_PRICE.multiply(GAS_LIMIT));
                    log.info("开始转移热钱包");
                    ColaChainWithdrawResponse response = TTCore.transaction2(TTCore.BITCOLA_ADDRESS, TTCore.getEthDecimal(realNumber), privateKey);
                    log.info(JSONObject.toJSONString(response));
                    log.info("转移结束");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void recordCurrentEthBlockNumber(long currentScanNumber) throws Exception {
        ttKeyMapper.addBlockNumber(currentScanNumber);
        Iterator<String> iterator = unConfirm.keySet().iterator();
        while (iterator.hasNext()){
            try {
                String hash = iterator.next();
                Integer oldConfirm = unConfirm.get(hash);
                ColaChainDepositResponse colaChainDepositResponse = depositMapper.selectByPrimaryKey(hash);
                String orderId = colaChainDepositResponse.getOrderId();
                if (oldConfirm+1>=super.getConfirmNumber(colaChainDepositResponse.getCoinCode())){
                    TTTransaction transaction = TTCore.getTransactionByHash(hash);
                    if (transaction.isStatus()){
                        iterator.remove();
                        colaChainDepositResponse.setStatus(DepositStatusConstant.CONFIRM);
                        depositMapper.updateByPrimaryKeySelective(colaChainDepositResponse);
                        completeDeposit(orderId);
                    }
                } else {
                    confirmNumber(oldConfirm + 1, orderId);
                    unConfirm.put(hash,oldConfirm+1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getModuleName() {
        return "TT";
    }

    /**
     *
     */
    @Override
    public String newAccount(String coinCode) throws Throwable {
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        Credentials credentials = TTCore.newAccount(password);
        ECKeyPair ecKeyPair = credentials.getEcKeyPair();
        BigInteger privateKey = ecKeyPair.getPrivateKey();
        BigInteger publicKey = ecKeyPair.getPublicKey();
        String account = credentials.getAddress();
        // 保存密钥
        ColaChainTTKey ethKey = new ColaChainTTKey();
        ethKey.setAddress(account);
        String privateKeyDecode = Numeric.toHexStringWithPrefix(privateKey);
        ethKey.setPrivateKey(AESUtil.encrypt(privateKeyDecode,encoderKey));
        ethKey.setPublicKey(Numeric.toHexStringWithPrefix(publicKey));
        ttKeyMapper.insert(ethKey);
        address.add(account);
        return account;
    }

    @Override
    public boolean checkAddress(String address) {
        return EthCore.checkAddress(address);
    }

    @Override
    public ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable {
        ColaChainNonce colaChainNonce = nonceMapper.selectByPrimaryKey(getModuleName());
        BigInteger dbNonce = new BigInteger(colaChainNonce.getNonce().toString());
        BigInteger nonce = TTCore.getEthNonce();
        if (colaChainNonce.getTimestamp() > System.currentTimeMillis() - 60 * 60 * 1000 && dbNonce.compareTo(nonce) >= 0){
            nonce = dbNonce;
        }
        ColaChainWithdrawResponse response;
        String contract =  ttTokenMapper.getToken(coinCode);
        ColaChainTTKey ttKey = ttKeyMapper.selectByPrimaryKey(TTCore.BITCOLA_ADDRESS);
        String privateKey = AESUtil.decrypt(ttKey.getPrivateKey(),encoderKey);
        if (getModuleName().equalsIgnoreCase(coinCode)&&contract==null){
            response = TTCore.transaction(address,number,privateKey,nonce);
        } else {
            response = TTCore.transactionToken(TTCore.BITCOLA_ADDRESS,address,number,contract,privateKey,nonce);
        }
        if (response.isSuccess()){
            response.setFeeCoinCode(getModuleName());
            colaChainNonce.setNonce(nonce.add(BigInteger.ONE).intValue());
            colaChainNonce.setTimestamp(System.currentTimeMillis());
            nonceMapper.updateByPrimaryKeySelective(colaChainNonce);
        }
        return response;
    }

    @Override
    public void confirm() throws Throwable {
    }

    @Override
    public ColaChainBalance getChainBalance(String coinCode, String feeCoinCode) throws Throwable {
        BigDecimal balance = TTCore.getBalance(TTCore.BITCOLA_ADDRESS);
        ColaChainBalance chainBalance = new ColaChainBalance();
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        chainBalance.setFeeBalance(balance);
        if (coinCode.equals(feeCoinCode)){
            chainBalance.setBalance(balance);
        } else {
            // BigDecimal tokenBalance = EthCore.getTokenBalance(EthCore.BITCOLA_ADDRESS, ethKeyMapper.getToken(coinCode));
            // TT代币,暂时不上
            chainBalance.setBalance(BigDecimal.ZERO);
        }
        return chainBalance;
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void transfer() throws Exception{
        // 查询最近一个月的有充值记录的地址 返回  地址+币种+合约+转移资金数 (已经去重)
        log.info("======================= tt 热钱包转移开始 =========================");
        List<Map<String,Object>> result = ttKeyMapper.
                getTransferInfo(System.currentTimeMillis()-(30*24*60*60*1000L),System.currentTimeMillis());
        log.info("最新一个月充值数:"+result.size());
        for (Map<String, Object> map : result) {
            String coin = map.get("coin").toString();
            String address = map.get("address").toString();
            ColaChainTTKey ethKey = ttKeyMapper.selectByPrimaryKey(address);
            String privateKey = AESUtil.decrypt(ethKey.getPrivateKey(),encoderKey);
            transferCoin(coin,privateKey, address);
        }
        log.info("======================= tt 热钱包转移结束 =========================");
    }

}
