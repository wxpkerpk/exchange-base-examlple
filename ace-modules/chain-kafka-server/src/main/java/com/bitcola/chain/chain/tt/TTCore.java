package com.bitcola.chain.chain.tt;


import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.entity.ColaChainEthKey;
import com.bitcola.chain.init.GlobalChain;
import com.bitcola.chain.util.AESUtil;
import com.bitcola.chain.util.HttpClientUtils;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/*
 * @author:wx
 * @description:
 * @create:2018-10-20  15:34
 */
@Log4j2
@Service
public class TTCore {

    private static final String WEB3_ADDRESS = "https://mainnet-rpc.thundercore.com";
    // 主账号地址，提币就是它
    public static final String BITCOLA_ADDRESS = "0xc21bba8dcfc17e8954c2cb2b84671d8be02dc370";
    public static final String TRANSACTION_API = "https://scan.thundercore.com/api/v1/address/"+BITCOLA_ADDRESS+"/transactions?limit=20";
    public static final String HASH_API = "https://scan.thundercore.com/api/v1/transactions/";

    private static Web3j web3j;

    public static Web3j getWeb3j(){
        if (web3j == null){
            web3j = Web3j.build(new HttpService(WEB3_ADDRESS));
        }
        return web3j;
    }


    /**
     * 查询入账交易
     * @return
     * @throws Exception
     */
    public static List<TTTransaction> getIncomeTransaction() throws Exception{
        String json = HttpClientUtils.get(TRANSACTION_API);
        List<TTTransaction> list = JSONObject.parseObject(json).getJSONArray("data").toJavaList(TTTransaction.class);
        Iterator<TTTransaction> iterator = list.iterator();
        while (iterator.hasNext()){
            TTTransaction next = iterator.next();
            if (!next.isStatus() || !next.getTo().equalsIgnoreCase(BITCOLA_ADDRESS) || next.getTTNumber().compareTo(BigDecimal.ZERO)<=0){
                iterator.remove();
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception{
        long confirmNumber = getConfirmNumber(getTransactionByHash("0x698971315823f3f3843af125d134afb4e91dbd88b983699f392cff1c8d51ca8b").getBlockNumber());
        System.out.println(confirmNumber);
    }





    public static TTTransaction getTransactionByHash(String hash) throws Exception {
        String json = HttpClientUtils.get(HASH_API + hash);
        return JSONObject.parseObject(json,TTTransaction.class);
    }

    public static long getConfirmNumber(long blockNumber) throws Exception{
        long value = getWeb3j().ethBlockNumber().send().getBlockNumber().longValue();
        return value - blockNumber;
    }


    public static Credentials newAccount(String password) throws Exception {
        String path = GlobalChain.JAR_PATH+"/tt/key";
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
        String fileName = WalletUtils.generateNewWalletFile(password, file, false);
        return WalletUtils.loadCredentials(password, path+"/"+fileName);
    }



    public static ColaChainWithdrawResponse transaction( String toAddress, BigDecimal number, String privateKey,BigInteger nonce){
        Web3j web3j = getWeb3j();
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        // 获取个人钱包
        //Credentials credentials = loadCredentials(fromAddress,password);

        Credentials credentials = Credentials.create(privateKey);

        EthSendTransaction ethSendTransaction = null;

        try {
            // 设置手续费 和交易数量
            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            BigInteger GAS_PRICE = Convert.toWei("19", Convert.Unit.GWEI).toBigInteger();
            BigInteger GAS_LIMIT = BigInteger.valueOf(90000L);
            BigInteger amount = Convert.toWei(number, Convert.Unit.ETHER).toBigInteger();

            // 创建交易
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, GAS_PRICE,GAS_LIMIT, toAddress,amount);

            // 对交易签名
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // 发送交易到链上
            ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            if (ethSendTransaction.hasError()) {
                response.setSuccess(false);
                response.setErrMessage(ethSendTransaction.getError().getMessage());
            } else {
                String transactionHash = ethSendTransaction.getTransactionHash();
                response.setSuccess(true);
                response.setHash(transactionHash);
                response.setFee(getEthDecimal(GAS_PRICE.multiply(GAS_LIMIT)));
                return response;
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            response.setSuccess(false);
            response.setErrMessage(e.getMessage());
        }
        return response;
    }

    /**
     * 默认手续费
     * @param toAddress
     * @param number         eth 数量
     * @return
     */
    public static ColaChainWithdrawResponse transaction2(String toAddress,BigDecimal number,String privateKey){
        Web3j web3j = getWeb3j();
        // 获取个人钱包
        //Credentials credentials = loadCredentials(fromAddress,password);
        Credentials credentials = Credentials.create(privateKey);
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        try {
            TransactionReceipt transactionReceipt = Transfer.sendFunds(
                    web3j, credentials, toAddress,
                   number, Convert.Unit.ETHER).send();
            response.setSuccess(true);
            response.setHash(transactionReceipt.getTransactionHash());
            response.setFee(getEthDecimal(transactionReceipt.getGasUsed()));
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrMessage(e.getMessage());
            log.error(e.getMessage(),e);
        }
        return response;
    }


    /**
     * ETH token转账
     * @param fromAddress 冲这个地址
     * @param toAddress   到这个地址
     * @param number        数量      eth 数量
     * @param contractAddress   合约地址
     * @return
     */
    public static ColaChainWithdrawResponse transactionToken(String fromAddress,String toAddress,BigDecimal number,String contractAddress,String privateKey,BigInteger nonce){
        Web3j web3j = getWeb3j();
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        // 获取个人钱包
        //Credentials credentials = loadCredentials(fromAddress,password);

        Credentials credentials = Credentials.create(privateKey);

        EthSendTransaction ethSendTransaction = null;

        try {
            // 设置手续费 和交易数量
            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            BigInteger GAS_PRICE = Convert.toWei("19", Convert.Unit.GWEI).toBigInteger();
            BigInteger GAS_LIMIT = BigInteger.valueOf(90000L);
            BigInteger amount = Convert.toWei(number, Convert.Unit.ETHER).toBigInteger();

            Function function = new Function(
                    "transfer",
                    Arrays.asList(new Address(toAddress), new Uint256(amount)),
                    Arrays.asList(new TypeReference<Type>() {
                    }));
            String encodedFunction = FunctionEncoder.encode(function);
            // 创建交易
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, GAS_PRICE,
                    GAS_LIMIT, contractAddress, encodedFunction);
            //RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, GAS_PRICE,
            //        GAS_LIMIT, contractAddress, encodedFunction);

            // 对交易签名
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // 发送交易到链上
            ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            // 解析返回的结果

            if (ethSendTransaction.hasError()) {
                response.setSuccess(false);
                response.setErrMessage(ethSendTransaction.getError().getMessage());
            } else {
                String transactionHash = ethSendTransaction.getTransactionHash();
                response.setSuccess(true);
                response.setHash(transactionHash);
                response.setFee(getEthDecimal(GAS_PRICE.multiply(GAS_LIMIT)));
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            response.setSuccess(false);
            response.setErrMessage(e.getMessage());
        }
        return response;
    }

    public static BigDecimal getTokenBalance(String address,String contract) throws Exception{
        Function function = new Function("balanceOf",
                Arrays.asList(new Address(address)),
                Arrays.asList(new TypeReference<Address>(){})
        );
        String encodedFunction = FunctionEncoder.encode(function);
        String value = TTCore.getWeb3j().ethCall(org.web3j.protocol.core.methods.request.Transaction.
                createEthCallTransaction(address, contract, encodedFunction), DefaultBlockParameterName.LATEST)
                .send().getValue();
        return TTCore.getEthDecimal(Numeric.decodeQuantity(value));
    }


    /**
     * 获取 input 中的数量
     * @param input
     * @return
     */
    public static BigDecimal getTokenNumber(String input){
        if (input.length() == 138){
            String number = input.substring(74);
            return Convert.fromWei(new BigInteger(number,16).toString(),Convert.Unit.ETHER);
        }
        return null;
    }

    public static BigDecimal getBalance(String address) throws Exception{
        BigInteger balance = getWeb3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
        return getEthDecimal(balance);
    }

    /**
     * 获取 input 中的收款地址
     * @return
     */
    public static String getTokenToAddress(String input){
        return  "0x"+input.substring(34, 74);
    }

    public static BigInteger getEthInteger(BigDecimal number){
        return Convert.toWei(number, Convert.Unit.ETHER).toBigInteger();
    }
    public static BigDecimal getEthDecimal(BigInteger number){
        return Convert.fromWei(number.toString(),Convert.Unit.ETHER);
    }

    public static BigInteger getEthNonce() throws Exception{
        EthGetTransactionCount ethGetTransactionCount = getWeb3j().ethGetTransactionCount(
                BITCOLA_ADDRESS, DefaultBlockParameterName.LATEST).sendAsync().get();
        return ethGetTransactionCount.getTransactionCount();
    }


}
