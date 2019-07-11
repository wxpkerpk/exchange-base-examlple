package com.bitcola.chain.server.eth;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.eth.EthCore;
import com.bitcola.chain.config.SpringContextsUtil;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.controller.ChainSendMessage;
import com.bitcola.chain.entity.*;
import com.bitcola.chain.mapper.*;
import com.bitcola.chain.server.BaseChainServer;
import com.bitcola.chain.util.AESUtil;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zkq
 * @create 2019-04-28 11:09
 **/
@Service
@Log4j2
public class EthChainServer extends BaseChainServer {
    public static ExecutorService executor = Executors.newFixedThreadPool(20);
    public static ExecutorService reScanThread = Executors.newFixedThreadPool(2);
    public static final Map<Long, Integer> ERROR_BLOCK = new ConcurrentHashMap<>();
    public static final Map<String, Integer> ERROR_HASH = new ConcurrentHashMap<>();

    @Autowired
    ColaChainDepositMapper depositMapper;

    @Autowired
    ColaChainKeyMapper keyMapper;

    @Autowired
    ColaChainEthKeyMapper ethKeyMapper;

    @Autowired
    ColaChainEthTokenMapper tokenMapper;

    @Autowired
    ColaChainEthNonceMapper nonceMapper;

    @Autowired
    ColaSmsEarlyWarningMapper warningMapper;

    @Autowired
    EthChainServer chainServer;

    @Value("${bitcola.chain.password}")
    String encoderKey;

    @Override
    public String getModuleName() {
        return "ETH";
    }

    @Override
    protected void run() throws Throwable {
        // 初始化,获得扫描的地址,开始扫描的区块,最新区块
        address.addAll(getAddress());
        List<ColaChainDepositResponse> unConfirmDeposit = depositMapper.unConfirm(getModuleName());
        for (ColaChainDepositResponse depositResponse : unConfirmDeposit) {
            unConfirm.put(depositResponse.getHash(), 0);
        }
        long currentScanNumber = ethKeyMapper.getStartBlockNumber();
        Web3j web3j = EthCore.getWeb3j();
        long latestBlockNumber = 0;
        try {
            latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (Exception e) {
            log.error("初始化错误,没有获得最新区块");
            run();
        }
        while (true) {
            try {
                if (latestBlockNumber != 0 && currentScanNumber >= latestBlockNumber) {
                    // 当前扫描到了最新的区块,等待5秒继续扫描
                    Thread.sleep(5 * 1000);
                    try {
                        latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber().longValue();
                    } catch (IOException e) {
                        log.error("获得最新区块失败,继续尝试,当前区块:" + latestBlockNumber);
                    }
                    continue;
                }
                if (currentScanNumber % 100 == 0) log.info("当前扫描区块: " + currentScanNumber);
                try {
                    scanBlock(currentScanNumber,executor);
                } catch (Exception e) {
                    log.info("扫描区块出错: " + currentScanNumber + " ,错误:" + e.getMessage() + ",当前错误池数量:" + ERROR_BLOCK.size());
                    ERROR_BLOCK.put(currentScanNumber, 0);
                }
                executor.shutdown();
                while (!executor.isTerminated()) {
                    Thread.sleep(30);
                }
                executor = Executors.newFixedThreadPool(20);
                // 存下当前扫描到的区块
                currentScanNumber++;
                recordCurrentEthBlockNumber(currentScanNumber);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void scanBlock(long currentScanNumber,ExecutorService executors) throws IOException {
        Request<?, EthBlock> ethBlockRequest =
                EthCore.getWeb3j().ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(currentScanNumber)), false);
        EthBlock ethBlock = ethBlockRequest.send();
        List<EthBlock.TransactionResult> transactions = ethBlock.getBlock().getTransactions();
        for (EthBlock.TransactionResult transaction : transactions) {
            String hash = transaction.get().toString();
            executors.submit(() -> {
                try {
                    scanTransaction(hash);
                } catch (Exception e) {
                    ERROR_HASH.put(hash, 0);
                    log.info("扫描hash出错: " + hash + " ,错误:" + e.getMessage() + ",当前错误池数量:" + ERROR_HASH.size());
                }
            });
        }
    }

    private void scanTransaction(String hash) throws IOException {
        Transaction result = EthCore.getWeb3j().ethGetTransactionByHash(hash).send().getResult();
        if (result == null) return;
        earlyWarning(result);
        if (address.contains(EthCore.getToAddress(result)) && EthCore.depositSuccess(result.getHash())) {
            boolean exists = depositMapper.existsWithPrimaryKey(hash);
            if (exists) return;
            if (!EthCore.isTokenTransaction(result)) {
                BigDecimal number = EthCore.getEthDecimal(EthCore.getNumber(result));
                if (getDepositMin(getModuleName()).compareTo(number) <= 0) {
                    ColaChainDepositResponse deposit = createDepositEntity(hash, number, getModuleName(), getModuleName(),
                            DepositStatusConstant.NOT_RECORD, result.getTo(), null);
                    String orderId = deposit(deposit);
                    if (StringUtils.isNotBlank(orderId)) {
                        unConfirm.put(hash, 0);
                        // 转移热钱包
                        cachedThreadPool.submit(() -> {
                            chainServer.transferCoinOrToken(null, getModuleName(), result.getTo(), getPrivateKey(result.getTo()));
                        });
                    }
                }
            } else {
                ColaChainEthToken ethToken = tokenMapper.getEthTokenByContract(EthCore.getContract(result));
                if (ethToken == null) return;
                BigDecimal tokenNumber = EthCore.getEthDecimal(EthCore.getNumber(result), ethToken.getUnit());
                if (tokenNumber.compareTo(getDepositMin(ethToken.getCoinCode())) >= 0) {
                    ColaChainDepositResponse deposit = createDepositEntity(hash, tokenNumber,ethToken.getCoinCode(), getModuleName(),
                            DepositStatusConstant.NOT_RECORD, EthCore.getToAddress(result), null);
                    String orderId = deposit(deposit);
                    if (StringUtils.isNotBlank(orderId)) {
                        unConfirm.put(hash, 0);
                        // 转移热钱包
                        cachedThreadPool.submit(() -> {
                            chainServer.transferCoinOrToken(ethToken.getContract(), ethToken.getCoinCode(), deposit.getToAddress(), getPrivateKey(deposit.getToAddress()));
                        });
                    }
                }
            }
        }
    }

    private void earlyWarning(Transaction result) {
        List<ColaSmsEarlyWarning> contractList = warningMapper.getWarningContract();
        String contract = result.getTo();
        for (ColaSmsEarlyWarning warning : contractList) {
            if (warning.getContract().equalsIgnoreCase(contract)){
                BigDecimal threshold = warning.getThreshold();
                Integer decimal = warning.getDecimal();
                BigDecimal number = EthCore.getEthDecimal(EthCore.getNumber(result), decimal);
                if (number.compareTo(threshold) >= 0){
                    ChainSendMessage sendMessage = SpringContextsUtil.applicationContext.getBean(ChainSendMessage.class);
                    // 短信预警
                    String url = "https://etherscan.io/tx/"+result.getHash();
                    String sms = warning.getSmsGroup() + "检测到大额交易:"+number.stripTrailingZeros().toPlainString() + ",点击查看:"+url;
                    List<String> tels = warningMapper.getWarningTelephoneByGroup(warning.getSmsGroup());
                    for (String tel : tels) {
                        sendMessage.smsEarlyWarning(sms,tel);
                        log.info(sms);
                    }
                }
            }
        }
    }

    /**
     * 区块数+1
     */
    private void recordCurrentEthBlockNumber(long blockNumber) {
        ethKeyMapper.addBlockNumber(blockNumber);
        Iterator<String> iterator = unConfirm.keySet().iterator();
        while (iterator.hasNext()) {
            try {
                String hash = iterator.next();
                Integer oldConfirm = unConfirm.get(hash);
                ColaChainDepositResponse colaChainDepositResponse = depositMapper.selectByPrimaryKey(hash);
                String orderId = colaChainDepositResponse.getOrderId();
                if (oldConfirm + 1 >= super.getConfirmNumber(colaChainDepositResponse.getCoinCode())) {
                    Transaction result = EthCore.getTransactionByHash(hash);
                    if (address.contains(EthCore.getToAddress(result))) {
                        if (EthCore.depositSuccess(result.getHash())) {
                            iterator.remove();
                            colaChainDepositResponse.setStatus(DepositStatusConstant.CONFIRM);
                            depositMapper.updateByPrimaryKeySelective(colaChainDepositResponse);
                            completeDeposit(orderId);
                        }
                    }
                } else {
                    confirmNumber(oldConfirm + 1, orderId);
                    unConfirm.put(hash, oldConfirm + 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String newAccount(String coinCode) throws Throwable {
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key, encoderKey);
        Credentials credentials = EthCore.newAccount(password);
        ECKeyPair ecKeyPair = credentials.getEcKeyPair();
        BigInteger privateKey = ecKeyPair.getPrivateKey();
        BigInteger publicKey = ecKeyPair.getPublicKey();
        String account = credentials.getAddress();
        // 保存密钥
        ColaChainEthKey ethKey = new ColaChainEthKey();
        ethKey.setAddress(account);
        String privateKeyDecode = Numeric.toHexStringWithPrefix(privateKey);
        ethKey.setPrivateKey(AESUtil.encrypt(privateKeyDecode, encoderKey));
        ethKey.setPublicKey(Numeric.toHexStringWithPrefix(publicKey));
        ethKeyMapper.insert(ethKey);
        address.add(account);
        return account;
    }

    @Override
    public boolean checkAddress(String address) {
        return EthCore.checkAddress(address);
    }

    @Override
    @Transactional
    public synchronized ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable {
        ColaChainNonce colaChainNonce = nonceMapper.selectByPrimaryKey(getModuleName());
        BigInteger dbNonce = new BigInteger(colaChainNonce.getNonce().toString());
        BigInteger nonce = EthCore.getEthNonce(EthCore.BITCOLA_ADDRESS);
        if (colaChainNonce.getTimestamp() > System.currentTimeMillis() - 60 * 60 * 1000 && dbNonce.compareTo(nonce) >= 0) {
            nonce = dbNonce;
        }
        ColaChainWithdrawResponse response = null;
        ColaChainEthKey ethKey = ethKeyMapper.selectByPrimaryKey(EthCore.BITCOLA_ADDRESS);
        String privateKey = AESUtil.decrypt(ethKey.getPrivateKey(), encoderKey);
        if (getModuleName().equalsIgnoreCase(coinCode)) {
            response = EthCore.transaction(address, EthCore.getEthInteger(number), privateKey, nonce);
        } else {
            ColaChainEthToken token = tokenMapper.selectByPrimaryKey(coinCode);
            String contract = token.getContract();
            Integer tokenUnit = token.getUnit();
            response = EthCore.transactionToken(address, EthCore.getEthInteger(number, tokenUnit), contract, privateKey, nonce);
        }
        if (response.isSuccess()) {
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
        ColaChainBalance chainBalance = new ColaChainBalance();
        BigDecimal balance = EthCore.getEthDecimal(EthCore.getEthBalance(EthCore.BITCOLA_ADDRESS));
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        chainBalance.setFeeBalance(balance);
        if (coinCode.equals(feeCoinCode)) {
            chainBalance.setBalance(balance);
        } else {
            ColaChainEthToken ethToken = tokenMapper.selectByPrimaryKey(coinCode);
            BigInteger tokenBalance = EthCore.getTokenBalance(EthCore.BITCOLA_ADDRESS, ethToken.getContract());
            chainBalance.setBalance(EthCore.getEthDecimal(tokenBalance, ethToken.getUnit()));
        }
        return chainBalance;
    }


    /**
     * 转移 到热钱包
     *
     * @param contract
     * @param coin
     * @param address
     * @param privateKey
     * @param bitcolaPrivateKey
     */
    BigDecimal fee = new BigDecimal("0.0025");

    @Transactional
    public void transferCoinOrToken(String contract, String coinCode, String address, String privateKey) {
        ColaChainCoin coin = getCoinCode(coinCode);
        log.info("=== 开始转移热钱包:" + coin.getCoinCode());
        try {
            BigDecimal balance = EthCore.getEthDecimal(EthCore.getEthBalance(address));
            if (getModuleName().equals(coin.getCoinCode())) {
                if (balance.compareTo(coin.getTransferToHotLimit()) > 0) {
                    EthGasPrice ethGasPrice = EthCore.getWeb3j().ethGasPrice().sendAsync().get();
                    BigInteger GAS_PRICE = ethGasPrice.getGasPrice();
                    BigInteger GAS_LIMIT = BigInteger.valueOf(21_000L);
                    BigInteger realNumber = EthCore.getEthInteger(balance).subtract(GAS_PRICE.multiply(GAS_LIMIT));
                    ColaChainWithdrawResponse response = EthCore.transaction2(EthCore.BITCOLA_ADDRESS, EthCore.getEthDecimal(realNumber), privateKey);
                    log.info(JSONObject.toJSONString(response));
                }
            } else {
                ColaChainEthToken ethToken = tokenMapper.selectByPrimaryKey(coin.getCoinCode());
                BigDecimal tokenBalance = EthCore.getEthDecimal(EthCore.getTokenBalance(address, contract), ethToken.getUnit());
                log.info("== 此币为代币,余额为:" + tokenBalance);
                if (tokenBalance.compareTo(coin.getTransferToHotLimit()) > 0) {
                    // 账户手续费够不够,不够充值,等待下一次扫描在转移到热钱包
                    if (balance.compareTo(fee) < 0) {
                        log.info("== 手续费不足,先转移手续费:" + fee);
                        ColaChainWithdrawResponse response = EthCore.transaction2(address, fee, getPrivateKey(EthCore.TRANSFER_ADDRESS));
                        log.info(JSONObject.toJSONString(response));
                        if (response.isSuccess()){
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    transferToken(address, tokenBalance, contract, privateKey, ethToken.getUnit());
                                }
                            }, 10 * 60 * 1000);
                        }
                    } else {
                        transferToken(address, tokenBalance, contract, privateKey, ethToken.getUnit());
                    }
                }
            }
            log.info("==== 转移结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void transferToken(String address, BigDecimal number, String contract, String privateKey, int tokenUnit) {
        try {
            ColaChainWithdrawResponse response = EthCore.transactionToken(EthCore.BITCOLA_ADDRESS, EthCore.getEthInteger(number, tokenUnit), contract, privateKey, EthCore.getEthNonce(address));
            log.info(JSONObject.toJSONString(response));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String getPrivateKey(String address) {
        ColaChainEthKey ethKey = ethKeyMapper.selectByPrimaryKey(address);
        return AESUtil.decrypt(ethKey.getPrivateKey(), encoderKey);
    }



    /**
     * 每1分钟扫描错误的区块和 hash
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void reScan() {
        Iterator<Long> iterator = ERROR_BLOCK.keySet().iterator();
        while (iterator.hasNext()) {
            Long errorBlock = iterator.next();
            if (ERROR_BLOCK.get(errorBlock) >= 3) {
                iterator.remove();
                return;
            }
            try {
                scanBlock(errorBlock,reScanThread);
                iterator.remove();
            } catch (Exception e) {
                int count = ERROR_BLOCK.get(errorBlock) + 1;
                if (count == 100) break;
                ERROR_BLOCK.put(errorBlock, count);
                log.info("再次扫描区块报错:" + errorBlock + ",当前重试次数:" + count);
            }
        }
        Iterator<String> hash = ERROR_HASH.keySet().iterator();
        while (hash.hasNext()) {
            String errorHash = hash.next();
            try {
                scanTransaction(errorHash);
                hash.remove();
            } catch (Exception e) {
                int count = ERROR_HASH.get(errorHash) + 1;
                if (count == 100) break;
                ERROR_HASH.put(errorHash, count);
                log.info("再次扫描hash报错:" + errorHash + ",当前重试次数:" + count);
            }
        }
    }

    public static void main(String[] args) throws Exception{
        String address="0x7c569b7d6f466183edff637ae55e7aab1e362537";
        String key="DD12138~!@#";
        String privateKey="aiWwnY6R4KMepo368lcsv4qrD8RtumAwCf9LOtAYDT45vQUMbbHURUGz6tlz2rIVMpQHtJO/YEwwWEXvTpxQAA7JfiBNatGoZUXfBrxeS1k=";
        String p = AESUtil.decrypt(privateKey, key);

        String hash = "0x2bd170defd3bf9856c25dfce1fd7a71d1ad806b2355984bf57cb26a20d8816fa";
        new EthChainServer().scanTransaction(hash);
    }


    @Scheduled(cron = "0 0 5 * * ?")
    public void transfer() throws Exception {
        log.info("======================= eth 自动热钱包转移开始 =========================");
        List<Map<String, String>> result = ethKeyMapper.
                getTransferInfo(System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L), System.currentTimeMillis());
        log.info("最新一个月充值数:" + result.size());
        for (Map<String, String> map : result) {
            String coin = map.get("coin").toString();
            String address = map.get("address").toString();
            String contract = map.get("contract");
            ColaChainEthKey ethKey = ethKeyMapper.selectByPrimaryKey(address);
            String privateKey = AESUtil.decrypt(ethKey.getPrivateKey(), encoderKey);
            log.info(address + " 币种:" + coin);
            chainServer.transferCoinOrToken(contract, coin, address, privateKey);
        }
        log.info("======================= eth 自动热钱包转移结束 =========================");
    }

}
