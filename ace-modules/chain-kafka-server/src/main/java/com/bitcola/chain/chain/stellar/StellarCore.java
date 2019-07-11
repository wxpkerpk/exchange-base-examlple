package com.bitcola.chain.chain.stellar;


import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.util.AESUtil;
import com.bitcola.chain.util.HttpClientUtils;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import org.apache.commons.lang3.StringUtils;
import org.stellar.sdk.*;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.requests.RequestBuilder;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.TransactionResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;


/**
 * @author zkq
 * @create 2019-01-16 12:05
 **/
public class StellarCore {

    private static String HOST = "https://horizon.stellar.org";
    private static String ACCOUNT_ID = "GAC2X2FBMLHKEDDZUROJJCHNF554LULJSHMKDTVHAIRVRHU2522MUJT5";
    private static Map<String,TransactionResponse> TRANSACTION_CACHE = new HashMap<>();
    private static final BigDecimal SCALE = new BigDecimal("10000000");
    private static Server server;
    private static Server getServer(){
        if (server == null){
            server = new Server(HOST);
        }
        return server;
    }

    public static String newAccount(){
        return ACCOUNT_ID;
    }

    /**
     *
     * @param toAccount
     * @param number
     * @param memo
     * @param tokenCode  此项不为空表示是代币交易
     * @param tokenIssuer
     * @param seed
     * @return
     */
    public static ColaChainWithdrawResponse withdraw(String toAccount, BigDecimal number, String memo, String tokenCode,
                                                     String tokenIssuer, String seed){
        Network.usePublicNetwork();
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        try {
            // 检查账号是否存在,不存在会抛出异常
            KeyPair destination;
            try {
                destination = KeyPair.fromAccountId(toAccount);
                getServer().accounts().account(destination);
            } catch (IOException e) {
                e.printStackTrace();
                response.setSuccess(false);
                response.setErrMessage("地址不存在:"+toAccount);
                return response;
            }
            // 加载自己账户的信息
            KeyPair source = KeyPair.fromSecretSeed(seed);
            AccountResponse sourceAccount = getServer().accounts().account(source);

            // 是否代币交易
            Asset asset;
            if (StringUtils.isBlank(tokenCode)){
                asset = new AssetTypeNative();
            } else {
                asset = Asset.createNonNativeAsset(tokenCode, KeyPair.fromAccountId(tokenIssuer));
            }
            // 创建交易
            Transaction transaction = new Transaction.Builder(sourceAccount)
                    .addOperation(
                            new PaymentOperation.Builder(destination,
                                    asset,
                                number.setScale(2,RoundingMode.DOWN).toString()
                    ).build())
                    .addMemo(Memo.text(memo))
                    .setTimeout(10)
                    .build();
            // 签名
            transaction.sign(source);
            SubmitTransactionResponse submit = getServer().submitTransaction(transaction);
            if (submit.isSuccess()){
                response.setSuccess(true);
                response.setHash(submit.getHash());
                response.setFee(getXLMFee(new BigDecimal(transaction.getFee())));
            } else {
                response.setSuccess(false);
                response.setErrMessage(JSONObject.toJSONString(submit));
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setErrMessage("区块链出错: "+e.getMessage());
        }
        return response;
    }

    public static List<XlmTransaction> getTransactions() throws Exception {
        PaymentsRequestBuilder paymentsRequest = getServer().payments().forAccount(KeyPair.fromAccountId(ACCOUNT_ID))
                .limit(20).order(RequestBuilder.Order.DESC);
        ArrayList<OperationResponse> records = paymentsRequest.execute().getRecords();
        List<XlmTransaction> list = new ArrayList<>();
        for (OperationResponse record : records) {
            if (record instanceof PaymentOperationResponse){
                PaymentOperationResponse payment = (PaymentOperationResponse)record;
                if (payment.getTo().getAccountId().equals(ACCOUNT_ID)){
                    XlmTransaction transaction = new XlmTransaction();
                    transaction.setFrom(payment.getFrom().getAccountId());
                    transaction.setTo(payment.getTo().getAccountId());
                    transaction.setAmount(new BigDecimal(payment.getAmount()));
                    transaction.setHash(payment.getTransactionHash());
                    // memo
                    TransactionResponse response = TRANSACTION_CACHE.get(transaction.getHash());
                    if (response == null){
                        response = getServer().transactions().transaction(transaction.getHash());
                        TRANSACTION_CACHE.put(transaction.getHash(),response);
                    }
                    if (response.getMemo() instanceof MemoText || response.getMemo() instanceof MemoId){
                        String memo;
                        if (response.getMemo() instanceof MemoText){
                            memo = ((MemoText) response.getMemo()).getText();
                        } else {
                            Long memoId = ((MemoId) response.getMemo()).getId();
                            memo = memoId.toString();
                        }
                        transaction.setMemo(memo);
                        // 代币
                        Asset asset = payment.getAsset();
                        if (asset instanceof AssetTypeNative){
                            transaction.setIsToken(false);
                        } else if (asset instanceof AssetTypeCreditAlphaNum){
                            transaction.setIsToken(true);
                            transaction.setTokenCode(((AssetTypeCreditAlphaNum) asset).getCode());
                            transaction.setTokenIssuer(((AssetTypeCreditAlphaNum) asset).getIssuer().getAccountId());
                        }
                        list.add(transaction);
                    }
                }
            }
        }
        return list;
    }


    private static void accountInfo(String accountId) throws Exception{
        AccountResponse account = getServer().accounts().account(KeyPair.fromAccountId(accountId));
        System.out.println("Balances for account " + accountId);
        for (AccountResponse.Balance balance : account.getBalances()) {
            System.out.println(String.format(
                    "Type: %s, Code: %s, Balance: %s",
                    balance.getAssetType(),
                    balance.getAssetCode(),
                    balance.getBalance()));
        }
    }

    public static BigDecimal getXLMBalance() throws Exception{
        AccountResponse account = getServer().accounts().account(KeyPair.fromAccountId(ACCOUNT_ID));
        for (AccountResponse.Balance balance : account.getBalances()) {
            Asset asset = balance.getAsset();
            if (asset instanceof AssetTypeNative){
                return new BigDecimal(balance.getBalance());
            }
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal getTokenBalance(String tokenCode, String tokenIssuer) throws Exception{
        AccountResponse account = getServer().accounts().account(KeyPair.fromAccountId(ACCOUNT_ID));
        for (AccountResponse.Balance balance : account.getBalances()) {
            Asset asset = balance.getAsset();
            if (asset instanceof AssetTypeNative){
            } else {
                String accountId = balance.getAssetIssuer().getAccountId();
                String assetCode = balance.getAssetCode();
                if (tokenCode.equals(assetCode) || tokenIssuer.equals(accountId)){
                    return new BigDecimal(balance.getBalance());
                }
            }
        }
        return BigDecimal.ZERO;
    }


    public static void createAccount() throws Exception{
        KeyPair pair = KeyPair.random();

        System.out.println(new String(pair.getSecretSeed()));
        System.out.println(pair.getAccountId());

        // 测试网络冲点钱
        String friendbotUrl = String.format(
                "https://friendbot.stellar.org/?addr=%s",
                pair.getAccountId());

        InputStream response = new URL(friendbotUrl).openStream();
        String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
        System.out.println("SUCCESS! You have a new account :)\n" + body);

        accountInfo(pair.getAccountId());
    }

    //public static void main(String[] args) throws Exception {
    //    //String seed = "";
    //    //String tokenIssuer = "GBBAMI2WU6WJHDL3CQKT4LPXUC76WCEMQJMJIVQGL2G5IKJ2JHEVHG3G";
    //    //trustToken("BTX",tokenIssuer,seed);
    //}

    /**
     * 信任该资产
     * @param tokenCode
     * @param tokenIssuer
     * @param seed
     * @return
     * @throws Exception
     */
    public static String trustToken(String tokenCode,String tokenIssuer,String seed){
        SubmitTransactionResponse submitTransactionResponse = null;
        try {
            // 查看自己的账户是否拥有此 token
            if (!trustedToken(tokenCode,tokenIssuer)){
                Network.usePublicNetwork();
                // 加载自己账户的信息
                KeyPair source = KeyPair.fromSecretSeed(seed);
                AccountResponse sourceAccount = getServer().accounts().account(source);

                Asset token = Asset.createNonNativeAsset(tokenCode, KeyPair.fromAccountId(tokenIssuer));

                Transaction allowAsset = new Transaction.Builder(sourceAccount)
                        .addOperation(
                                new ChangeTrustOperation.Builder(token, "707382697076").build())
                        .setTimeout(10)
                        .build();
                allowAsset.sign(source);
                submitTransactionResponse = getServer().submitTransaction(allowAsset);
                return submitTransactionResponse.getHash();
            } else {
                return "已经拥有了此 token";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean trustedToken(String tokenCode,String tokenIssuer) throws IOException{
        AccountResponse account = getServer().accounts().account(KeyPair.fromAccountId(ACCOUNT_ID));
        for (AccountResponse.Balance balance : account.getBalances()) {
            if (tokenCode.equalsIgnoreCase(balance.getAssetCode())
                    && tokenIssuer.equalsIgnoreCase(balance.getAssetIssuer().getAccountId())){
                return true;
            }
        }
        return false;
    }

    public static boolean hashSuccess(String hash) throws Exception {
        String s = HttpClientUtils.get("https://horizon.stellar.org/transactions/" + hash);
        String hash1 = null;
        try {
            JSONObject object = JSONObject.parseObject(s);
            hash1 = object.getString("hash");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return StringUtils.isNotBlank(hash1);
    }

    private static BigDecimal getXLMFee(BigDecimal fee){
        return fee.divide(SCALE);
    }

    public static boolean checkAddress(String address) {
        return address.length() == 56;
    }

}
