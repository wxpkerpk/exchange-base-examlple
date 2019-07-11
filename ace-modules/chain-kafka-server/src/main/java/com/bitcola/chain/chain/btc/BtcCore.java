package com.bitcola.chain.chain.btc;

/*
 * @author:wx
 * @description:
 * @create:2018-10-21  13:24
 */

import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.usdt.UsdtCore;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.java_websocket.util.Base64;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


@Service
public class BtcCore {

    private static String rpc_user = "bitcola";
    private static String rpc_password = "bitcola";
    private static String rpc_url = "http://192.168.0.87:61315";

    private static JsonRpcHttpClient btcClient;

    /**
     * 验证地址是否存在
     * @param address
     * @return
     * @throws Throwable
     */
    public static Object validateaddress(String address)throws Throwable{
        return getClient().invoke("validateaddress", new Object[] {address}, Object.class);
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

    /**
     * 转账到制定的账户中
     * @param address
     * @param amount
     * @return
     * @throws Throwable
     */
    public static String sendtoaddress(String address, BigDecimal amount,String password)throws Throwable{
        walletpassphase(password,30);
        return  getClient().invoke("sendtoaddress", new Object[] {address,amount,"","",false}, Object.class).toString();
    }

    /**
     * 查询账户下的交易记录
     * @param count
     * @param offset
     * @return
     * @throws Throwable
     */
    public static Object getTransactions(int count ,int offset )throws Throwable{
        Object transactions = getClient().invoke("listtransactions", new Object[]{"*", count, offset}, Object.class);
        return transactions;
    }

    public static String getBalance() throws Throwable{
        return  getClient().invoke("getbalance", new Object[] {}, Object.class).toString();
    }
    public static String getBalanceByAddress(String address) throws Throwable{
        return  getClient().invoke("getbalance", new Object[] {address}, Object.class).toString();
    }

    /**
     * 获取地址下未花费的币量

     * @return
     * @throws Throwable
     */
    public static String listunspent( int minconf ,int maxconf ,String address)throws Throwable{
        String[] addresss= new String[]{address};
        return  (String) getClient().invoke("listunspent", new Object[] {minconf,maxconf,addresss}, Object.class).toString();
    }

    /**
     * 生成新的接收地址
     * @return
     * @throws Throwable
     */
    public static String getNewAddress() throws Throwable{
        return  (String) getClient().invoke("getnewaddress", new Object[] {}, Object.class).toString();
    }

    /**
     * 获取钱包信息
     * @return
     * @throws Throwable
     */
    public static String getInfo() throws Throwable{
        return  getClient().invoke("getinfo", new Object[] {}, Object.class).toString();
    }

    public static Object getTransaction(String hash) throws Throwable{
        return  getClient().invoke("gettransaction", new Object[] {hash}, Object.class);
    }

    private static JsonRpcHttpClient getClient() throws Exception{
        if (btcClient == null){
            String cred = Base64.encodeBytes((rpc_user + ":" + rpc_password).getBytes());
            Map<String, String> headers = new HashMap<String, String>(1);
            headers.put("Authorization", "Basic " + cred);
            btcClient =  new JsonRpcHttpClient(new URL(rpc_url), headers);
        }
        return btcClient;
    }

    public static boolean checkAddress(String address) {
        try {
            Object validateaddress = validateaddress(address);
            return JSONObject.parseObject(JSONObject.toJSONString(validateaddress)).getBooleanValue("isvalid");
        } catch (Throwable throwable) {
        }
        return true;
    }



}