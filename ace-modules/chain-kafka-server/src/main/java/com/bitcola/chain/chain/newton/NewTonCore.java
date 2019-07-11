package com.bitcola.chain.chain.newton;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.newton.entity.NewTonTransaction;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.util.HttpClientUtils;
import com.bitcola.chain.util.MemoUtil;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.nem.core.utils.HexEncoder;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

/**
 * @author zkq
 * @create 2019-04-09 14:52
 **/
@Data
public class NewTonCore {

    public static final String ADDRESS = "NEW182bUyQybs1TZYkVKiKZE4PzqNx1pSrHrJGD";
    public static final String GET_TRANSACTION_API = "https://explorer.newtonproject.org/api/v1/txs?address="+ADDRESS+"&pageNum=0";
    public static final String GET_ACCOUNT_API = "https://explorer.newtonproject.org/api/v1/addr/"+ADDRESS+"/?noTxList=1";
    public static final String HASH_API = "https://explorer.newtonproject.org/api/v1/tx/";

    public static List<NewTonTransaction> getIncomeTransaction() throws Exception{
        String json = HttpClientUtils.get(GET_TRANSACTION_API);
        List<NewTonTransaction> txs = JSONObject.parseObject(json).getJSONArray("txs").toJavaList(NewTonTransaction.class);
        Iterator<NewTonTransaction> iterator = txs.iterator();
        while (iterator.hasNext()){
            NewTonTransaction next = iterator.next();
            if (next.isFrom_contract() || next.isTo_contract() || next.getValue().compareTo(BigDecimal.ZERO) <=0 || !next.getTo_addr().equalsIgnoreCase(ADDRESS)){
                iterator.remove();
            }
        }
        return txs;
    }

    public static void main(String[] args) throws Exception{
        boolean dev = false;
        List<NewTonTransaction> list = NewTonCore.getIncomeTransaction();
        for (NewTonTransaction transaction : list) {
            String memo = NewTonCore.getMemo(transaction.getData());
            if ((!dev && MemoUtil.isProdMemo(memo)) || (dev && MemoUtil.isDevMemo(memo))) {
                String hash = transaction.getTxid();
                BigDecimal number = transaction.getValue();
                //boolean exists = depositMapper.existsWithPrimaryKey(hash);
                if (!false && number.compareTo(new BigDecimal(100))>=0){
                    ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                    deposit.setHash(hash);
                    deposit.setAmount(number);
                    deposit.setTimestamp(System.currentTimeMillis());
                    deposit.setModule("new");
                    deposit.setCoinCode("new");
                    deposit.setToAddress(transaction.getTo_addr());
                    deposit.setStatus(DepositStatusConstant.NOT_RECORD);
                    deposit.setMemo(memo);
                    //String orderId = deposit(deposit);
                    //if (StringUtils.isNotBlank(orderId)){
                    //    unConfirm.put(hash,0);
                    //}
                    System.out.println(number);

                }
            }
        }

    }


    public static boolean hashSuccess(String hash) throws Exception{
        String json = HttpClientUtils.get(HASH_API + hash);
        return JSONObject.parseObject(json).getBigDecimal("confirmations").compareTo(BigDecimal.ONE)>=0;
    }

    public static BigDecimal getBalance() throws Exception{
        String json = HttpClientUtils.get(GET_ACCOUNT_API);
        return JSONObject.parseObject(json).getBigDecimal("balance");
    }



    public static String getMemo(String hexString){
        if (hexString == null || "0x".equals(hexString)) return "";
        byte[] bytes = new byte[0];
        try {
            if (hexString.length() > 2 && hexString.startsWith("0x")){
                hexString = hexString.substring(2);
            }
            bytes = HexEncoder.getBytes(hexString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(bytes);
    }


    public static boolean checkAddress(String address) {
        if (!address.startsWith("NEW")) return false;
        return address.length() == 39;
    }

}
