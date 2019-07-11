package com.bitcola.chain.chain.eos;


import client.EosApiClientFactory;
import client.EosApiRestClient;
import client.domain.response.chain.AbiJsonToBin;
import client.domain.response.chain.Block;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.eos.entity.*;
import com.bitcola.chain.chain.tera.entity.Account;
import com.bitcola.chain.util.AESUtil;
import com.bitcola.chain.util.HttpClientUtils;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import com.bitcola.exchange.security.common.util.TimeUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 交易流程  签署 abi --> 创建 action --> 签署交易(这里是自己的钱包) --> 提交交易
 *
 * 区块链浏览器 : https://eosflare.io
 *
 * @author zkq
 * @create 2018-12-07 18:46
 **/
@Log4j2
public class EosCore {
    public static final String QUERY_URL = "https://api.eospark.com/api";       // eospark 的接口
    public static final String API_KEY = "361e152f1df4c249d6655221eba5d541";    // eospark 的key

    public static final String LOCALHOST = "http://s3:9800"; //
    public static final String HOSTS = "https://api.eossweden.se";              // 一个飞快的超级节点
    public static final String PUBLIC_KEY = "EOS5jduYX8A7g5YMh5nx6EbcY1xeEMFRUuz6u5z3Hf6tGp3G6NoLf";
    public static final String ADDRESS = "bitcolaex.e";
    public static final String ACTION = "transfer";
    public static final String BLOCK = "aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906";//正式网络

    public static EosApiRestClient eosApiRestClient = EosApiClientFactory.newInstance(HOSTS).newRestClient();



    /**
     * 查询
     * @param address
     * @param page
     * @param size
     * @return
     * @throws Exception
     */
    public static List<EosTransaction> getTransaction(String address, int page, int size) throws Exception{
        StringBuilder params = new StringBuilder("?module=account&action=get_account_related_trx_info&transaction_type=1&apikey="+API_KEY);
        params.append("&account="+address);
        params.append("&page="+page);
        params.append("&size="+size);
        String result = HttpClientUtils.get(QUERY_URL + params.toString());
        JSONObject object = JSONObject.parseObject(result);
        Integer errno = object.getInteger("errno");
        if (errno == 0){
            EosTransactions transactions = object.getObject("data", EosTransactions.class);
            List<EosTransaction> trace_list = transactions.getTrace_list();
            Iterator<EosTransaction> iterator = trace_list.iterator();
            while (iterator.hasNext()){
                EosTransaction next = iterator.next();
                String status = next.getStatus();
                if (!status.equals("executed")){
                    iterator.remove();
                }
            }
            return transactions.getTrace_list();
        }
        return new ArrayList<>();
    }

    public static BigDecimal getBalance(String code,String account,String symbol){
        List<String> balance = eosApiRestClient.getCurrencyBalance(code, account, symbol);
        if (balance.size() > 0){
            String replace = balance.get(0).replace(symbol, "");
            return new BigDecimal(replace.trim());
        }
        return BigDecimal.ZERO;
    }



    /**
     *
     * @param address
     * @param memo
     * @param tokenName 这个具体的 symbol 需要从合约中取
     * @param number    数量
     * @param symbol  合约名称
     * @param precision  精度(每个代币都有不同的精度控制,错误的精度无法转账)
     * @return
     */
    public static ColaChainWithdrawResponse withdraw(String address, String memo, String symbol, BigDecimal number, String tokenName, int precision, String key) {
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        try {
            checkResource(key);
            // 签署 abi
            String transactionAbi = getTransactionAbi(ADDRESS, address, number, memo,symbol,precision,tokenName);
            // 创建 action
            Action transactionAction = getTransactionAction(transactionAbi,tokenName);
            // 签署交易
            TransactionParam transactionParam = signTransaction(transactionAction,key);
            // 提交交易
            return pushTransaction(transactionParam, transactionAction);
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setFee(BigDecimal.ZERO);
            response.setErrMessage(e.getMessage());
            return response;
        }
    }






    /**
     *  交易签名 (必须本地钱包签名)
     * @param action
     * @return
     * @throws Exception
     */
    synchronized public static TransactionParam signTransaction(Action action,String key) throws Exception{
        lockWallet();
        List<Object> trans = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        EosChainInfo chainInfo = getChainInfo(HOSTS);
        String headBlockNum = chainInfo.getHeadBlockNum();
        Block block = eosApiRestClient.getBlock(headBlockNum);
        long ti = TimeUtils.getDate(block.getTimeStamp()).getTime() + 1000 * 60 * 20;
        String expireTime = TimeUtils.getUTC0String(ti);
        map.put("ref_block_num",headBlockNum);
        map.put("ref_block_prefix",block.getRefBlockPrefix().toString());
        map.put("expiration",expireTime);
        map.put("actions", Arrays.asList(action));
        map.put("signatures",new ArrayList<>());
        trans.add(map);
        trans.add(Arrays.asList(PUBLIC_KEY));
        trans.add(BLOCK);
        unlockWallet(key);
        String s = HttpClientUtils.postParameters(LOCALHOST + "/v1/wallet/sign_transaction", JSONObject.toJSONString(trans));
        String signatures = null;
        try {
            signatures = JSONObject.parseObject(s).getJSONArray("signatures").get(0).toString();
            lockWallet();
        } catch (NullPointerException e) {
            Integer code = JSONObject.parseObject(s).getJSONObject("error").getInteger("code");
            if (code == 3120004){
                unlockWallet(key);
                s = HttpClientUtils.postParameters(LOCALHOST + "/v1/wallet/sign_transaction", JSONObject.toJSONString(trans));
                signatures = JSONObject.parseObject(s).getJSONArray("signatures").get(0).toString();
                lockWallet();
            }
        }
        TransactionParam param = new TransactionParam();
        param.setSignatures(signatures);
        param.setExpiration(expireTime);
        param.setRef_block_num(Long.valueOf(headBlockNum));
        param.setRef_block_prefix(Long.valueOf(block.getRefBlockPrefix().toString()));
        return param;
    }

    /**
     * 提交交易到区块链上
     * @param param
     * @param action
     * @return
     * @throws Exception
     */
    public static ColaChainWithdrawResponse pushTransaction(TransactionParam param, Action action) throws Exception{
        Map<String,Object> map = new HashMap<>();
        map.put("compression","none");
        Map<String,Object> transaction = new HashMap<>();
        transaction.put("expiration",param.getExpiration());
        transaction.put("ref_block_num",Long.valueOf(param.getRef_block_num()));
        transaction.put("ref_block_prefix",Long.valueOf(param.getRef_block_prefix()));
        transaction.put("context_free_actions",new ArrayList<>());
        transaction.put("transaction_extensions",new ArrayList<>());
        transaction.put("actions",Arrays.asList(action));
        map.put("transaction",transaction);
        map.put("signatures",Arrays.asList(param.getSignatures()));
        String s = HttpClientUtils.postParameters(HOSTS+"/v1/chain/push_transaction", JSONObject.toJSONString(map));
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        String transaction_id = JSONObject.parseObject(s).getString("transaction_id");
        response.setSuccess(StringUtils.isNotBlank(transaction_id));
        response.setHash(transaction_id);
        response.setErrMessage(s);
        return response;
    }



     public static String getTransactionAbi(String from,String to,BigDecimal number,String memo,String symbol,int percent,String tokenName){
        Map<String,Object> args = new HashMap<>();
        args.put("from",from);
        args.put("to",to);
        args.put("quantity", getEosPercentString(number,percent,symbol));
        args.put("memo",memo);
        AbiJsonToBin abiJsonToBin = eosApiRestClient.abiJsonToBin(tokenName, ACTION, args);
        return abiJsonToBin.getBinargs();
     }

    public static Action getTransactionAction(String bin,String coinName){
        return getAction(bin,coinName,ACTION);
    }

    public static Action getAction(String bin,String coinName,String name){
        Action action = new Action();
        action.setData(bin);
        action.setAccount(coinName);
        action.setName(name);
        Map<String, String> actionAuth = new HashMap<>();
        actionAuth.put("actor",ADDRESS);
        actionAuth.put("permission","active");
        action.setAuthorization(Arrays.asList(actionAuth));
        return action;
    }

    public static boolean lockWallet() throws Exception{
        String s = HttpClientUtils.postParameters(LOCALHOST + "/v1/wallet/lock", "\"default\"");
        return true;
    }
    public static boolean unlockWallet(String key) throws Exception{
        String params = JSONObject.toJSONString(Arrays.asList("default",key) );
        String s = HttpClientUtils.postParameters(LOCALHOST + "/v1/wallet/unlock", params);
        if ("{}".equals(s)){
            return true;
        }
        return false;
    }


    /**
     * 这下面是资源购买的代码
     * @return
     */
    public static boolean stake(BigDecimal net,BigDecimal cpu,String key){
        String id = null;
        try {
            String binargs = getStakeAbi(net,cpu);
            Action action = getStakeAction(binargs);
            TransactionParam s = signTransaction(action,key);
            id =  pushTransaction(s,action).getHash();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("抵押资源出错");
        }
        return StringUtils.isNotBlank(id);
    }

    public static Action getStakeAction(String bin){
        return getAction(bin,"eosio","delegatebw");
    }
    public static String getStakeAbi(BigDecimal net, BigDecimal cpu) throws Exception{
        Delegatebw delegatebw = new Delegatebw();
        delegatebw.setCode("eosio");
        delegatebw.setAction("delegatebw");
        Map<String,Object> args = new HashMap<>();
        args.put("from",ADDRESS);
        args.put("receiver",ADDRESS);
        args.put("stake_net_quantity", getEosPercentString(net, 4,"EOS"));
        args.put("stake_cpu_quantity", getEosPercentString(cpu, 4,"EOS"));
        args.put("transfer",0);
        delegatebw.setArgs(args);
        String s = HttpClientUtils.postParameters(HOSTS + "/v1/chain/abi_json_to_bin", JSONObject.toJSONString(delegatebw));
        String binargs = JSONObject.parseObject(s).getString("binargs");
        return binargs;
    }

    /**
     * 检查资源是否充足,不足则自动购买
     * @return
     * @throws Exception
     */
    public static boolean checkResource(String key) throws Exception{
        JSONObject account = getAccount(ADDRESS);
        JSONObject net_limit = account.getJSONObject("net_limit");
        JSONObject cpu_limit = account.getJSONObject("cpu_limit");
        Long net_max = net_limit.getLong("max");
        Long net_available = net_limit.getLong("available");
        Long cpu_max = cpu_limit.getLong("max");
        Long cpu_available = cpu_limit.getLong("available");
        if (net_available.doubleValue()/net_max < 0.2){
            // 网络资源不够
            stake(new BigDecimal(5),BigDecimal.ZERO,key);
        }
        if (cpu_available.doubleValue()/cpu_max < 0.2){
            // cpu 资源不够
            stake(BigDecimal.ZERO,new BigDecimal(50),key);
        }
        return true;
    }



    private static EosChainInfo getChainInfo(String host) throws Exception{
        String result = HttpClientUtils.get(host + "/v1/chain/get_info");
        EosChainInfo eosChainInfo = JSONObject.parseObject(result, EosChainInfo.class);
        return eosChainInfo;
    }

    private static String getEosPercentString(BigDecimal number, int percent,String symbol){
        if (number == null) return "0.000 EOS";
        return number.setScale(percent, RoundingMode.DOWN).toString()+" "+symbol;
    }

    private static JSONObject getAccount(String address) throws Exception{
        String s = HttpClientUtils.postParameters(HOSTS + "/v1/chain/get_account", "{\"account_name\":\"" + address + "\"}");
        return JSONObject.parseObject(s);
    }
    public static String getTransaction(String hash) throws Exception{
        return HttpClientUtils.postParameters(HOSTS + "/v1/history/get_transaction", "{\"id\":\"" + hash + "\"}");
    }
    public static boolean hashSuccess(String hash) throws Exception{
        return true;
    }

    public static boolean checkAddress(String address) {
        try {
            JSONObject account = getAccount(address);
            String accountName = account.getString("account_name");
            return address.equalsIgnoreCase(accountName);
        } catch (Exception e) {
        }
        return true;
    }


}
