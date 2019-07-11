package com.bitcola.chain.chain.tera;


import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.tera.entity.Account;
import com.bitcola.chain.chain.tera.entity.History;
import com.bitcola.chain.chain.tera.entity.HistoryTransaction;
import com.bitcola.chain.chain.tera.entity.SendResult;
import com.bitcola.chain.util.AESUtil;
import com.bitcola.chain.util.HttpClientUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author zkq
 * @create 2019-03-07 20:19
 **/
public class TeraCore {
    public static final String withdraw = "-";
    public static final String deposit = "+";
    private static final BigDecimal scale = new BigDecimal("1000000000");

    private static final String HOST = "http://192.168.0.87:15211";

    private static final String GET_HISTORY = "/api/v2/GetHistoryTransactions";
    private static final String SEND = "/api/v2/Send";
    private static final String QUERY_HASH = "/api/v2/GetTransaction";
    private static final String GENERATE_KEYS = "/api/v2/GenerateKeys";
    private static final String CREATE_ACCOUNT = "/api/v2/CreateAccount";
    private static final String GET_BALANCE = "/api/v2/GetBalance";

    public static final long accountId = 193548;

    /**
     * 创建一个新账号
     * @return
     */
    public static Account newAccount(){
        Account account = new Account();
        try {
            String keyStr = HttpClientUtils.postParameters(HOST + GENERATE_KEYS, "");
            JSONObject keys = JSONObject.parseObject(keyStr);
            String privateKey = keys.getString("PrivKey");
            String publicKey = keys.getString("PubKey");
            account.setPrivateKey(privateKey);
            account.setPublicKey(publicKey);
            String params = String.format("{\"Name\": \"bitcolawallet\",\"PrivKey\": \"%s\",\"Wait\":1}", privateKey);
            String accountStr = HttpClientUtils.postParameters(HOST + CREATE_ACCOUNT, params);
            JSONObject accounts = JSONObject.parseObject(accountStr);
            account.setResult(accounts.getIntValue("result"));
            account.setAccountID(accounts.getIntValue("result"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return account;
    }

    public static void main(String[] args) {
        List<History> transactions = getTransactions(accountId);
        for (History transaction : transactions) {
            System.out.println(transaction.getTxID());
        }
    }


    /**
     * 获得最新100条交易记录
     * @param accountId
     * @return
     */
    public static List<History> getTransactions(long accountId){
        try {
            String params = String.format("{\"AccountID\": %d,\"GetTxID\": 1,\"GetDescription\": 1}",accountId );
            String data = HttpClientUtils.postParameters(HOST + GET_HISTORY, params);
            HistoryTransaction transaction = JSONObject.parseObject(data, HistoryTransaction.class);
            return transaction.getHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 发送
     * @param fromID
     * @param privateKey
     * @param toID
     * @param amount
     * @return
     */
    public static SendResult send(long fromID, String privateKey,long toID,BigDecimal amount){
        try {
            String param = "{\"FromID\": %d,\"FromPrivKey\": \"%s\",\"ToID\": %d,\"Amount\": %f,\"Description\": \"bitcola\",\"Wait\": 1}";
            String params = String.format(param,fromID,privateKey,toID,amount);
            String data = HttpClientUtils.postParameters(HOST + SEND, params);
            return JSONObject.parseObject(data, SendResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            SendResult result = new SendResult();
            result.setText(e.getMessage());
            return result;
        }
    }


    /**
     * 判断 交易 hash 是否成功
     * @param hash
     * @return
     */
    public static boolean hashSuccess(String hash){
        try {
            String params = String.format("{\"TxID\": \"%s\"}", hash);
            String data = HttpClientUtils.postParameters(HOST + QUERY_HASH, params);
            int result = JSONObject.parseObject(data).getIntValue("result");
            if (result == 1) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取余额
     * @param accountId
     * @return
     */
    public static BigDecimal getBalance(long accountId){
        try {
            String params = String.format("{\"AccountID\": %d}", accountId);
            String data = HttpClientUtils.postParameters(HOST + GET_BALANCE, params);
            JSONObject jsonObject = JSONObject.parseObject(data);
            BigDecimal sumCOIN = jsonObject.getBigDecimal("SumCOIN");
            BigDecimal sumCENT = jsonObject.getBigDecimal("SumCENT");
            return sumCOIN.add(getSumCENT(sumCENT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获得小数部分的数量
     * @param SumCENT
     * @return
     */
    public static BigDecimal getSumCENT(BigDecimal SumCENT){
        if (SumCENT.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return SumCENT.divide(scale);
    }


    public static boolean checkAddress(String address) {
        try {
            Long.valueOf(address);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
