package com.bitcola.chain.chain.usdt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.util.AESUtil;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.java_websocket.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-01-10 17:46
 **/
@Component
public class UsdtCore{

    private static String rpc_user = "bitcola";
    private static String rpc_password = "BitColaUsdtPassWord";
    private static String rpc_url = "http://192.168.0.85:8332";
    public static final int propertyId = 31;

    private static JsonRpcHttpClient usdtClient;

    // 收取手续费的地址 (如果)
    public static final String redeemAddress = "1JZu6GfjCCnRuNuNtgoDT9CaofNeog9iWc";
    // 从哪个地址
    public static final String fromAddress = "1JZu6GfjCCnRuNuNtgoDT9CaofNeog9iWc";

    public static String getUsdtInfo() throws Throwable{
        return  (String) getClient().invoke("omni_getinfo", new Object[]{}, Object.class).toString();
    }

    public static String newAccount() throws Throwable{
        return  (String) getClient().invoke("getnewaddress", new Object[]{}, Object.class);
    }
    public static String transfer(String from,String toAddress, BigDecimal number,int propertyId,String password) throws Throwable{
        walletpassphase(password,30);
        return  getClient().invoke("omni_funded_send", new Object[]{from,toAddress,propertyId,
                number.toPlainString(),redeemAddress }, Object.class).toString();
    }

    public static String transferAll(String from,String toAddress,BigDecimal number, int propertyId,String password) throws Throwable{
        walletpassphase(password,30);
        return  getClient().invoke("omni_funded_sendall", new Object[]{from,toAddress,1,
                redeemAddress }, Object.class).toString();
    }
    public static String getTransaction(String hash) throws Throwable{
        return  JSONObject.toJSONString(getClient().invoke("omni_gettransaction", new Object[]{hash}, Object.class));
    }

    /**
     * 获取 最新的交易记录
     * @return
     * @throws
     */
    public static String getTransactions(int size,int offset) throws Throwable{
        return JSONObject.toJSONString(getClient().invoke("omni_listtransactions", new Object[]{"*",size,offset}, Object.class));
    }

    /**
     * 获取 最新的交易记录
     * @return
     * @throws
     */
    public static String getAddressBalance() throws Throwable{
        return JSONObject.toJSONString(getClient().invoke("omni_getwalletaddressbalances", new Object[]{}, Object.class));
    }

    public static BigDecimal getBalance(String address) throws Throwable{
        String json = JSONObject.toJSONString(getClient().invoke("omni_getbalance", new Object[]{address,propertyId}, Object.class));
        return JSONObject.parseObject(json).getBigDecimal("balance");
    }

    public static BigDecimal getBtcBalance() throws Throwable{
        String balance = getClient().invoke("getbalance", new Object[] {}, Object.class).toString();
        return  new BigDecimal(balance);
    }

    /**
     * 如果钱包加密需要临时解锁钱包
     * @param password
     * @param time
     * @return
     * @throws Throwable
     */
    public static void walletpassphase(String password,int time)throws Throwable{
        getClient().invoke("walletpassphrase", new Object[] {password,time}, Object.class);
    }
    private static JsonRpcHttpClient getClient() throws Exception{
        if (usdtClient == null){
            String cred = Base64.encodeBytes((rpc_user + ":" + rpc_password).getBytes());
            Map<String, String> headers = new HashMap<String, String>(1);
            headers.put("Authorization", "Basic " + cred);
            usdtClient =  new JsonRpcHttpClient(new URL(rpc_url), headers);
        }
        return usdtClient;
    }


    /**
     * 将币种全部汇总到热钱包
     * @return
     */
    public static void sum(String password) {
        try {
            String addressBalance = UsdtCore.getAddressBalance();
            List<UsdtBalanceEntity> jsonObjects = JSONArray.parseArray(addressBalance, UsdtBalanceEntity.class);
            for (UsdtBalanceEntity balanceEntity : jsonObjects) {
                List<Balance> balances = balanceEntity.getBalances();
                for (Balance balance : balances) {
                    int propertyid = balance.getPropertyid();
                    if (propertyid == 31 && balance.getBalance().compareTo(BigDecimal.TEN)>=0 && !balanceEntity.getAddress().equals(UsdtCore.fromAddress)){
                        String hash = UsdtCore.transfer(balanceEntity.getAddress(), UsdtCore.fromAddress, balance.getBalance(), propertyId, password);
                        System.out.println(balanceEntity.getAddress()+" 数量:"+balance.getBalance());
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


}
