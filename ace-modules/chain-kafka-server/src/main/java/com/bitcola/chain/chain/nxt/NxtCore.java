package com.bitcola.chain.chain.nxt;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.nxt.entity.Transaction;
import com.bitcola.chain.util.HttpClientUtils;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author zkq
 * @create 2019-03-12 09:49
 **/
public class NxtCore {

    public static final String ACCOUNT_ID = "NXT-Z6WA-Y9Y9-8ZAU-F99J3";
    public static final String HOST = "http://192.168.0.85:7876/nxt";
    public static final BigDecimal SCALE = new BigDecimal("100000000");

    public static String newAccount(){
        return ACCOUNT_ID;
    }


    public static List<Transaction> getTransactions(int size,String password){
        try {
            String json = HttpClientUtils.get(HOST + getTransactionsParams(ACCOUNT_ID, size));
            List<Transaction> transactions = JSONObject.parseObject(json).getJSONArray("transactions").toJavaList(Transaction.class);
            Iterator<Transaction> iterator = transactions.iterator();
            while (iterator.hasNext()){
                Transaction transaction = iterator.next();


                if (transaction.getSenderRS().equals(ACCOUNT_ID)){
                    iterator.remove();
                } else {
                    if (transaction.getAttachment().getMessage() == null && transaction.getAttachment().getEncryptedMessage() != null){
                        String message = decryptMessage(password, transaction.getSenderRS(), transaction.getAttachment().getEncryptedMessage().getData(),
                                transaction.getAttachment().getEncryptedMessage().getNonce());
                        transaction.getAttachment().setMessage(message);
                    }
                    if (StringUtils.isBlank(transaction.getAttachment().getMessage())){
                        iterator.remove();
                    }
                }

            }
            return transactions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static Transaction getTransaction(String hash){
        try {
            String json = HttpClientUtils.get(HOST + getTransactionParams(hash));
            return JSONObject.parseObject(json, Transaction.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Transaction();
    }

    public static ColaChainWithdrawResponse send(String toAccount, BigDecimal number, String memo, String password){
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        BigDecimal minFeeNQT = getMinFeeNQT(toAccount, number, memo, password);
        String json = null;
        try {
            String params = getSendMoneyParams(getSecretPhraseUrl(password), toAccount, getNqtNumber(number).longValue(), minFeeNQT.longValue(), memo);
            String url =HOST + params;
            json = HttpClientUtils.postParameters(url,"");
            JSONObject jsonObject = JSONObject.parseObject(json);
            String hash = jsonObject.getString("transaction");
            response.setSuccess(true);
            response.setHash(hash);
            response.setFee(getNxtNumber(minFeeNQT));
            //boolean broadcasted = jsonObject.getBooleanValue("broadcasted"); // 是否广播
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setErrMessage(json);
        }
        return response;
    }

    public static BigDecimal getBalance(){
        try {
            String json = HttpClientUtils.get(HOST + getBalanceParams(ACCOUNT_ID));
            BigDecimal balanceNQT = JSONObject.parseObject(json).getBigDecimal("balanceNQT");
            return getNxtNumber(balanceNQT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }


    private static String getTransactionsParams(String account,int size){
        return String.format("?requestType=getBlockchainTransactions&account=%s&lastIndex=%d",account,size-1);
    }

    private static String getTransactionParams(String hash){
        return String.format("?requestType=getTransaction&transaction=%s",hash);
    }

    private static String getSendMoneyParams(String secretPhrase,String recipient,long amountNQT,long feeNQT,String message){
        return String.format("?requestType=sendMoney&secretPhrase=%s&recipient=%s&amountNQT=%d&feeNQT=%d&deadline=1440&message=",secretPhrase,recipient,amountNQT,feeNQT,message);
    }
    private static String getBalanceParams(String account){
        return String.format("?requestType=getBalance&account=%s",account);
    }

    /**
     * / 1亿
     * @return
     */
    public static BigDecimal getNxtNumber(BigDecimal nqtNumber){
        return nqtNumber.divide(SCALE);
    }

    /**
     * * 1亿
     * @return
     */
    public static BigDecimal getNqtNumber(BigDecimal nxtNumber){
        return nxtNumber.multiply(SCALE).setScale(0, RoundingMode.DOWN);
    }

    /**
     * 转移 助记词
     * @return
     */
    private static String getSecretPhraseUrl(String secretPhrase){
        return secretPhrase.replace(" ","%20");
    }

    /**
     * 获得最小费率
     * @return
     */
    private static BigDecimal getMinFeeNQT(String toAccount,BigDecimal number,String memo,String password){
        try {
            String params = getSendMoneyParams(getSecretPhraseUrl(password), toAccount, getNqtNumber(number).longValue(), 0, memo);
            String url =HOST + params + "&broadcast=false";
            String json = HttpClientUtils.postParameters(url,"");
            JSONObject.parseObject(json).getJSONObject("transactionJSON").getBigDecimal("feeNQT");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ONE.multiply(SCALE);
    }

    /**
     * 广播事务
     * @param transactionBytes
     * @return
     */
    private static String broadcastTransaction(String transactionBytes){
        try {
            String url = HOST + "?requestType=broadcastTransaction&transactionBytes="+transactionBytes;
            String json = HttpClientUtils.postParameters(url, "");
            return JSONObject.parseObject(json).getString("transaction");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decryptMessage(String secretPhrase,String account,String data,String nonce){
        try {
            String url = HOST + String.format("?requestType=decryptFrom&secretPhrase=%s&account=%s&data=%s&nonce=%s",getSecretPhraseUrl(secretPhrase),account,data,nonce);
            String json = HttpClientUtils.get(url);
            return JSONObject.parseObject(json).getString("decryptedMessage");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkAddress(String address) {
        return address.startsWith("NXT-");
    }
}
