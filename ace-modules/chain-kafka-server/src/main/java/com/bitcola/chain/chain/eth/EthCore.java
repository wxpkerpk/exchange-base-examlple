package com.bitcola.chain.chain.eth;


import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.init.GlobalChain;
import com.bitcola.chain.util.AESUtil;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * @author:wx
 * @description:
 * @create:2018-10-20  15:34
 */
@Log4j2
@Service
public class EthCore {

    public static final String BITCOLA_ADDRESS = "0x7c569b7d6f466183edff637ae55e7aab1e362537";
    public static final String TRANSFER_ADDRESS = "0xf65b9c6dcbb04452babf1f2e10f414ea4de4d629";
    private static final String WEB3_ADDRESS = "https://mainnet.infura.io/v3/4e0b995349724fb28e3fbb7b26023120";
    private static final String WEB3_PROXY_ADDRESS = "https://www.bitcola.app/infura/v3/4e0b995349724fb28e3fbb7b26023120";
    private static final String ETHERSCAN_APIKEY = "6U92KGK2KTXAP1Y11FYBWV1QT16GGQDGU5";
    public static final String ETHSCAN_API = "https://api.etherscan.io/api";

    /**
     * 测试地址和私钥
     */
    private static final String WEB3_TEST_ADDRESS = "https://ropsten.infura.io/v3/4e0b995349724fb28e3fbb7b26023120";
    private static final String TEST_ADDRESS = "0x211a09ace3000ae571de2e6487ade0203add9a55";
    private static final String PRIVATE_KEY = "0xd1599990ab946c319082c156b0246f5c8362068da5a07ca4704fdb4877ad1ef1";


    private static Web3j web3j;

    public static Web3j getWeb3j(){
        if (web3j == null){
            web3j = Web3j.build(new HttpService(WEB3_PROXY_ADDRESS));
        }
        return web3j;
    }


    /**
     * 查看此 hash 之前的余额和之后的余额是否相差当前数值
     * @param hash
     * @return
     */
    public static boolean depositSuccess(String hash) throws IOException{
        Transaction transaction = getTransactionByHash(hash);
        BigInteger blockNumber = transaction.getBlockNumber();
        String contract = getContract(transaction);
        BigInteger number = getNumber(transaction);
        // 获得这个区块之前的 to 地址的余额,再获当前区块余额,比较
        BigInteger sub;
        if (StringUtils.isNotBlank(contract)){
            String toAddress = getTokenToAddress(transaction.getInput());
            BigInteger oldTokenBalance = getTokenBalance(toAddress, contract, DefaultBlockParameter.valueOf(blockNumber.subtract(BigInteger.ONE)));
            BigInteger tokenBalance = getTokenBalance(toAddress, contract, DefaultBlockParameter.valueOf(blockNumber));
            sub = tokenBalance.subtract(oldTokenBalance);
        } else {
            String toAddress = transaction.getTo();
            BigInteger oldTokenBalance = getEthBalance(toAddress, DefaultBlockParameter.valueOf(blockNumber.subtract(BigInteger.ONE)));
            BigInteger tokenBalance = getEthBalance(toAddress, DefaultBlockParameter.valueOf(blockNumber));
            sub = tokenBalance.subtract(oldTokenBalance);
        }
        if (sub.compareTo(number) == 0 ) return true;
        log.info("--> "+hash+" 存在异常,余额差值为:"+EthCore.getEthDecimal(sub));
        return false;
    }


    public static Transaction getTransactionByHash(String hash) throws IOException{
        return getWeb3j().ethGetTransactionByHash(hash).send().getResult();
    }


    public static Credentials newAccount(String password) throws Exception{
        String path = GlobalChain.JAR_PATH+"/eth/key";
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
        String fileName = WalletUtils.generateNewWalletFile(password, file, false);
        return WalletUtils.loadCredentials(password, path+"/"+fileName);
    }



    public static ColaChainWithdrawResponse transaction(String toAddress, BigInteger number, String privateKey,BigInteger nonce){
        Web3j web3j = getWeb3j();
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        try {
            // 验证余额
            Credentials credentials = Credentials.create(privateKey);
            BigInteger balance = getEthBalance(credentials.getAddress());
            if (number.compareTo(balance) >= 0){
                response.setSuccess(false);
                response.setErrMessage("ETH余额不足");
                return response;
            }
            // 获取个人钱包
            //Credentials credentials = loadCredentials(fromAddress,password);
            EthSendTransaction ethSendTransaction = null;
            // 设置手续费 和交易数量
            BigInteger GAS_PRICE = Convert.toWei("19", Convert.Unit.GWEI).toBigInteger();
            BigInteger GAS_LIMIT = BigInteger.valueOf(90000L);

            // 创建交易
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, GAS_PRICE,GAS_LIMIT, toAddress,number);

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
            response.setErrMessage(e.getLocalizedMessage());
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
     * @param toAddress   到这个地址
     * @param number        数量      eth 数量
     * @param contractAddress   合约地址
     * @return
     */
    public static ColaChainWithdrawResponse transactionToken(String toAddress,BigInteger number,String contractAddress,String privateKey,BigInteger nonce){
        Web3j web3j = getWeb3j();
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        // 获取个人钱包
        //Credentials credentials = loadCredentials(fromAddress,password);
        Credentials credentials = Credentials.create(privateKey);
        EthSendTransaction ethSendTransaction = null;
        try {
            BigInteger tokenBalance = getTokenBalance(credentials.getAddress(), contractAddress);
            if (tokenBalance.compareTo(number) < 0){
                response.setSuccess(false);
                response.setErrMessage("ETH token 余额不足");
                return response;
            }
            // 设置手续费 和交易数量
            BigInteger GAS_PRICE = Convert.toWei("21", Convert.Unit.GWEI).toBigInteger();
            BigInteger GAS_LIMIT = BigInteger.valueOf(90000L);

            Function function = new Function(
                    "transfer",
                    Arrays.asList(new Address(toAddress), new Uint256(number)),
                    Arrays.asList(new TypeReference<Type>() {
                    }));
            String encodedFunction = FunctionEncoder.encode(function);
            // 创建交易
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, GAS_PRICE,
                    GAS_LIMIT, contractAddress, encodedFunction);

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
            response.setErrMessage(e.getLocalizedMessage());
        }
        return response;
    }

    public static BigInteger getTokenBalance(String address,String contract) throws IOException{
        return getTokenBalance(address,contract,DefaultBlockParameterName.LATEST);
    }
    public static BigInteger getTokenBalance(String address,String contract,DefaultBlockParameter blockNumber) throws IOException{
        Function function = new Function("balanceOf",
                Arrays.asList(new Address(address)),
                Arrays.asList(new TypeReference<Address>(){})
        );
        String encodedFunction = FunctionEncoder.encode(function);
        String value = EthCore.getWeb3j().ethCall(org.web3j.protocol.core.methods.request.Transaction.
                createEthCallTransaction(address, contract, encodedFunction), blockNumber)
                .send().getValue();
        return Numeric.decodeQuantity(value);
    }

    public static BigInteger getEthBalance(String address, DefaultBlockParameter blockNumber) throws IOException{
        return getWeb3j().ethGetBalance(address,blockNumber).send().getBalance();
    }
    public static BigInteger getEthBalance(String address) throws IOException{
        return getEthBalance(address,DefaultBlockParameterName.LATEST);
    }


    /**
     * 获取 input 中的数量
     * @param input
     * @return
     */
    private static BigInteger getTokenNumber(String input){
        if (input.length() == 138){
            String number = input.substring(74);
            return new BigInteger(number,16);
        }
        return null;
    }


    /**
     * 获取 input 中的收款地址
     * @return
     */
    private static String getTokenToAddress(String input){
        return  "0x"+input.substring(34, 74);
    }

    /**
     * BigDecimal  --> BigInteger
     * @param number
     * @param unit 单位,默认是 18
     * @return
     */
    public static BigInteger getEthInteger(BigDecimal number,int unit){
        return number.multiply(BigDecimal.TEN.pow(unit)).toBigInteger();
    }
    public static BigInteger getEthInteger(BigDecimal number){
        return getEthInteger(number,18);
    }

    /**
     * BigInteger  --> BigDecimal
     * @param number
     * @param unit 单位,默认是 18
     * @return
     */
    public static BigDecimal getEthDecimal(BigInteger number,int unit){
        BigDecimal factor = BigDecimal.TEN.pow(unit);
        return new BigDecimal(number.toString()).divide(factor);
    }
    public static BigDecimal getEthDecimal(BigInteger number){
        return getEthDecimal(number,18);
    }



    public static BigInteger getEthNonce(String address) throws Exception{
        EthGetTransactionCount ethGetTransactionCount = getWeb3j().ethGetTransactionCount(
                address, DefaultBlockParameterName.LATEST).sendAsync().get();
        return ethGetTransactionCount.getTransactionCount();
    }


    public static boolean checkAddress(String address) {
        if (StringUtils.isBlank(address)) return false;
        address = address.toLowerCase();
        if (address.length() != 42) return false;
        try {
            Numeric.decodeQuantity(address);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String getToAddress(Transaction transaction){
        String input = transaction.getInput();
        if (input.length() == 138){
            return getTokenToAddress(input);
        } else if (input.equals("0x")){
            return transaction.getTo();
        }
        return null;
    }
    public static BigInteger getNumber(Transaction transaction){
        String input = transaction.getInput();
        if (input.length() == 138){
            return getTokenNumber(input);
        } else if (input.equals("0x")){
            return transaction.getValue();
        }
        return null;
    }
    public static boolean isTokenTransaction(Transaction transaction){
        return transaction.getInput().length() == 138 ;
    }
    public static String getContract(Transaction transaction){
        if (isTokenTransaction(transaction)) return transaction.getTo();
        return null;
    }

    public static void main(String[] args) throws Exception{
        String toAddress = "0x6a77e7a94F4346a7f2D7a5d5874200ed84A26e1C";
        String contract = "0xf23444084c75b15d76414633abb003d855df4605";
        String address = "0xf8d97c88f90bb16823b0482d9387a02f5d7020ce";
        String privateKey = "";
        //BigInteger tokenBalance = getTokenBalance(address, contract);
        //System.out.println(getEthDecimal(tokenBalance));

        ColaChainWithdrawResponse response = transactionToken(toAddress, getEthInteger(new BigDecimal("10000")), contract, privateKey, getEthNonce(address));
        System.out.println(JSONObject.toJSONString(response));

    }

    public static void test(String[] args) throws Exception{
        String toAddress = "0x6a77e7a94F4346a7f2D7a5d5874200ed84A26e1C";
        String contract = "0xf23444084c75b15d76414633abb003d855df4605";
        String address = "0xf8d97c88f90bb16823b0482d9387a02f5d7020ce";
        String privateKey = AESUtil.decrypt("","");
        //BigInteger tokenBalance = getTokenBalance(address, contract);
        //System.out.println(getEthDecimal(tokenBalance));

        ColaChainWithdrawResponse response = transactionToken(toAddress, getEthInteger(new BigDecimal("10")), contract, privateKey, getEthNonce(BITCOLA_ADDRESS));
        System.out.println(JSONObject.toJSONString(response));


    }


    /**
     * 生成一个账号
     * @throws Exception
     */
    private static void createAccount(String passowrd,String encoderPassword) throws Exception{
        GlobalChain.JAR_PATH = "/Users/qiuqiu/mywork/my";
        Credentials credentials = newAccount(passowrd);
        ECKeyPair ecKeyPair = credentials.getEcKeyPair();
        System.out.println("地址: "+credentials.getAddress());
        BigInteger privateKey = ecKeyPair.getPrivateKey();
        BigInteger publicKey = ecKeyPair.getPublicKey();
        String privateKeyDecode = Numeric.toHexStringWithPrefix(privateKey);
        System.out.println("公钥: "+Numeric.toHexStringWithPrefix(publicKey));
        System.out.println("私钥: "+AESUtil.encrypt(privateKeyDecode, encoderPassword));
    }


    /**
     * 发行 token
     *
     * @param data https://www.jianshu.com/p/9e6c17c47efb  // 教程地址 (直接看我下面写的教程即可)
     *             http://remix.ethereum.org/#optimize=false&version=soljson-v0.4.21+commit.dfe3193c.js // 在线编辑器
     *             https://github.com/ConsenSys/Tokens/tree/master/contracts/eip20 // 合约地址
     *
     *   发行步骤 1 在线编译合约代码 (需要翻墙),获得 abi 和 bytecode
     *                编译成功后点击右边 Details ,在弹出框中找到第五个大块 里面有 abi -> WEB3DEPLOY (5行) , bytecode -> WEB3DEPLOY (13行)
     *
     *           2 下载官方节点 https://geth.ethereum.org/downloads/
     *
     *           3  # 导入有eth的账户，要用这个账户来部署合约呢,private.key 为私钥转 16 进制文本文件,这个账号保证有 0.05 ETH及以上
     *              ./geth account import private.key
     *              # 启动孤立的节点
     *              ./geth --nodiscover console --rpc --rpcapi 'web3,eth,debug' --rpcport 8545 --rpccorsdomain '*' #
     *              # 查看导入的账户是第几位
     *              > eth.accounts
     *
     *           4 依次在 eth 控制台运行下面代码
     *
     *
     *      > var abi = [{.....}]  // 编译后的合约 WEB3DEPLOY (5行)
     *      > var bytecode = '0x6060604xxxxxxb0029' // 编译后的data  WEB3DEPLOY (13行)
     *      > var c = eth.contract(abi)
     *      > var cData = {from:eth.accounts[1], data:bytecode} //部署账户
     *      > var dcode = c.new.getData('100000000', 'tokenName', 18, 'SYMBOL', cData) // token数量,名字,精度(一般是 18 位),代号
     *      > decode
     *
     *           5 将获得的 decode 传入以下方法的 data 值运行
     *
     * @param address
     * @param privateKey
     * @throws Exception
     */
    public static void createToken(String data,String address,String privateKey) throws Exception{
        Credentials credentials = Credentials.create(privateKey);

        // 设置手续费 和交易数量
        BigInteger GAS_PRICE = Convert.toWei("9", Convert.Unit.GWEI).toBigInteger();
        BigInteger GAS_LIMIT = BigInteger.valueOf(5000000L);

        // 创建交易
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                getEthNonce(address), GAS_PRICE,GAS_LIMIT, null,data);

        // 对交易签名
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        // 发送交易到链上
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
        System.out.println(JSONObject.toJSONString(ethSendTransaction));
    }

}
