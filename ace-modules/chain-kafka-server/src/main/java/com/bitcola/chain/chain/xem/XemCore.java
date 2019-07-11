package com.bitcola.chain.chain.xem;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.xem.entity.*;
import com.bitcola.chain.util.HttpClientUtils;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import io.nem.apps.api.AccountApi;
import io.nem.apps.api.NamespaceMosaicsApi;
import io.nem.apps.api.TransactionApi;
import io.nem.apps.builders.ConfigurationBuilder;
import io.nem.apps.builders.TransferTransactionBuilder;
import io.nem.apps.factories.AttachmentFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.function.Add;
import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.PrivateKey;
import org.nem.core.messages.PlainMessage;
import org.nem.core.model.Account;
import org.nem.core.model.Address;
import org.nem.core.model.mosaic.Mosaic;
import org.nem.core.model.mosaic.MosaicFeeInformation;
import org.nem.core.model.mosaic.MosaicId;
import org.nem.core.model.ncc.AccountMetaDataPair;
import org.nem.core.model.ncc.NemAnnounceResult;
import org.nem.core.model.ncc.TransactionMetaDataPair;
import org.nem.core.model.primitive.Amount;
import org.nem.core.model.primitive.Quantity;
import org.nem.core.time.TimeInstant;
import org.nem.core.time.UnixTime;
import org.nem.core.utils.HexEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutionException;


/**
 * nembex (XEM) 这个币好像只能有一个账户,所以是个带 memo 的币种
 *      XPX 代币是 mosaics,所以查询 xpx 就只需要查询 mosaics 就可以了
 *
 *      区块链浏览器 http://chain.nem.ninja/
 * @author zkq
 * @create 2018-11-19 16:13
 **/
@Log4j2
public class XemCore {

    public static String ADDRESS = "NDUW4AKIJ2SI7P3TD6T2RY24H3Y3Y47S7GJGP2BU";
    public static String PUBLIC_KEY = "88b4b2ceb3f733422a9d7e1f27f64ac708a400413e886d357c58584219679226";
    public static BigDecimal XEM_SCALE = new BigDecimal(1000000);
    public static int SINGLE = 257; // 单签名

    public static String baseUrl = "http://192.168.0.87:7890";


    static {
        ConfigurationBuilder.nodeNetworkName("mainnet")
                .nodeNetworkProtocol("http")
                .nodeNetworkUri("192.168.0.87")
                .nodeNetworkPort("7890")
                .setup();
    }


    public static BigDecimal getXemBalance() throws Exception{
        AccountMetaDataPair accountByAddress = AccountApi.getAccountByAddress(ADDRESS);
        Amount balance = accountByAddress.getEntity().getBalance();
        return new BigDecimal(balance.getNumNem());
    }

    public static BigDecimal getMosaicBalance(String mosaicIdString) throws Exception{
        List<Mosaic> accountOwnedMosaic = AccountApi.getAccountOwnedMosaic(ADDRESS);
        for (Mosaic mosaic : accountOwnedMosaic) {
            if (getMosaicIdString(mosaic).equals(mosaicIdString)){
                return new BigDecimal(mosaic.getQuantity().getRaw()).divide(XEM_SCALE);
            }
        }
        return BigDecimal.ZERO;
    }


    /**
     * 新账户
     * @return
     * @throws Exception
     */
    public static String newAccount() throws Exception{
        return ADDRESS;
    }

    public static void main(String[] args) {
        List<XemTransactionLog> xemTransactionLogs = transfersIncoming(ADDRESS, null, null);
        for (XemTransactionLog xemTransactionLog : xemTransactionLogs) {

        }
    }

    /**
     * 获取最新25条交易信息(充值)
     *
     * @param address   地址 必填 其实就是我们的地址  如果没有后面两项,返回最新25条交易记录
     * @param hash  可选,返回这个 hash 之前的25条交易记录 相当于分页
     * @param id    可选,返回这个 id 之前的25条交易记录
     * @return
     * @throws Exception
     */
    public static List<XemTransactionLog> transfersIncoming(String address, String hash, String id){
        String uri = baseUrl + "/account/transfers/incoming?address=" + address;
        if (StringUtils.isNotBlank(hash)){
            uri += "&hash="+hash;
        }
        if (StringUtils.isNotBlank(id)){
            uri += "&id="+id;
        }
        return parseConfirmTransaction(uri);
    }

    public static List<XemTransactionLog> unConfirm(String address) throws Exception{
        String uri = baseUrl + "/account/unconfirmedTransactions?address=" + address;
        String result = HttpClientUtils.get(uri);
        JSONObject object = JSONObject.parseObject(result);
        JSONArray data = object.getJSONArray("data");
        List<XemTransactionLog> list = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            UnConfirmTransaction transaction = data.getObject(i, UnConfirmTransaction.class);
            if(StringUtils.isNotBlank(transaction.getTransaction().getMessage().getPayload())){
                XemTransactionLog xem = new XemTransactionLog();
                xem.setTxId(transaction.getMeta().getData());
                xem.setConfirm(false);
                parseTransaction(list, transaction.getTransaction(), xem);
            }
        }
        return parseConfirmTransaction(uri);
    }


    /**
     * 获取最新25条交易信息(out)
     *
     * @param address   地址 必填
     * @param hash      交易 hash 可选
     * @param id        事务 id   可选
     * @return
     * @throws Exception
     */
    public static List<XemTransactionLog> transfersOutgoing(String address,String hash,String id) throws Exception{
        String uri = baseUrl + "/account/transfers/outgoing?address=" + address;
        if (StringUtils.isNotBlank(hash)){
            uri += "&hash="+hash;
        }
        if (StringUtils.isNotBlank(id)){
            uri += "&id="+id;
        }
        return parseConfirmTransaction(uri);
    }
    /**
     * 获取最新25条交易信息(all)
     *
     * @param address   地址 必填
     * @param hash      交易 hash 可选
     * @param id        事务 id   可选
     * @return
     * @throws Exception
     */
    public static List<XemTransactionLog> transfersAll(String address,String hash,String id) throws Exception{
        String uri = baseUrl + "/account/transfers/all?address=" + address;
        if (StringUtils.isNotBlank(hash)){
            uri += "&hash="+hash;
        }
        if (StringUtils.isNotBlank(id)){
            uri += "&id="+id;
        }
        return parseConfirmTransaction(uri);
    }

    /**
     * 提币
     * @param address 目标地址
     * @param memo    备注
     * @param number  提币数量
     * @param key
     * @return
     */
    public static ColaChainWithdrawResponse withdraw(String address, String memo, BigDecimal number, String mosaicIdString, String key){
        if (StringUtils.isBlank(memo)){
            memo = "BitCola";
        }
        double minFee;
        if (StringUtils.isBlank(mosaicIdString)){
            minFee = getMinFee(number.doubleValue(),memo,MosaicId.parse(mosaicIdString),false);
        } else {
            minFee = getMinFee(number.doubleValue(),memo,null,true);
        }
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        try {
            NemAnnounceResult nemAnnounceResult;
            if (StringUtils.isBlank(mosaicIdString)){
                nemAnnounceResult = TransferTransactionBuilder.sender(new Account(new KeyPair(PrivateKey
                        .fromHexString(key))))
                        .recipient(new Account(Address.fromEncoded(address)))
                        .fee(Amount.fromMicroNem((long)(minFee*XEM_SCALE.doubleValue())))
                        .amount(Amount.fromMicroNem(number.multiply(XEM_SCALE).longValue()))
                        .attachment(AttachmentFactory.createTransferTransactionAttachmentMessage(new PlainMessage(memo.getBytes())))
                        .buildAndSendTransaction();
            } else {
                MosaicId mosaicId = MosaicId.parse(mosaicIdString);
                nemAnnounceResult = TransferTransactionBuilder.sender(new Account(new KeyPair(PrivateKey
                        .fromHexString(key))))
                        .recipient(new Account(Address.fromEncoded(address)))
                        .fee(Amount.fromMicroNem((long)(minFee*XEM_SCALE.doubleValue())))
                        .amount(Amount.fromNem(1))
                        .attachment(AttachmentFactory.createTransferTransactionAttachmentMessage(new PlainMessage(memo.getBytes())))
                        .addMosaic(mosaicId,new Quantity(number.multiply(XEM_SCALE).longValue()))
                        .buildAndSendTransaction();
            }
            boolean error = nemAnnounceResult.isError();
            String hash = nemAnnounceResult.getTransactionHash().toString();
            response.setSuccess(!error);
            response.setFee(new BigDecimal(minFee));
            response.setHash(hash);
            response.setErrMessage(JSONObject.toJSONString(nemAnnounceResult));
            log.info(JSONObject.toJSONString(nemAnnounceResult));
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setErrMessage("区块链出错:"+e.getMessage());
        }
        return response;
    }

    /**
     * 根据 hash 查询记录
     * @param hash
     * @param coinCode
     * @return
     */
    public static XemTransactionLog hash(String hash,String coinCode){
        try {
            TransactionMetaDataPair transaction = TransactionApi.getTransaction(hash);
            XemTransactionLog log = new XemTransactionLog();
            log.setTxId(hash);
            log.setConfirm(true);
            log.setFees(new BigDecimal(transaction.getEntity().getFee().getNumNem()));
            BigDecimal number = JSONObject.parseObject(JSONObject.toJSONString(transaction.getEntity())).getJSONObject("amount").getBigDecimal("numNem");
            log.setNumber(number);
            return log;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean hashSuccess(String hash) throws Exception{
        TransactionMetaDataPair transaction = TransactionApi.getTransaction(hash);
        return StringUtils.isNotBlank(transaction.getMetaData().getId().toString());
    }

    /**
     * 解析返回数据
     * @param uri
     * @return
     */
    public static List<XemTransactionLog> parseConfirmTransaction(String uri){
        List<XemTransactionLog> list = new ArrayList<>();
        try {
            String result = HttpClientUtils.get(uri);
            JSONObject object = JSONObject.parseObject(result);
            JSONArray data = object.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                TransatationEntity transaction = data.getObject(i, TransatationEntity.class);
                XemTransactionLog xem = new XemTransactionLog();
                xem.setTxId(transaction.getMeta().getHash().getData());
                xem.setHashId(String.valueOf(transaction.getMeta().getId()));
                xem.setConfirm(true);
                parseTransaction(list, transaction.getTransaction(), xem);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    private static void parseTransaction(List<XemTransactionLog> list, Transaction transaction, XemTransactionLog xem) {
        if (UnixTime.fromTimeInstant(new TimeInstant(transaction.getTimeStamp().intValue())).getMillis()>1545984877000L){
            if (transaction.getType() == SINGLE){
                if (transaction.getMosaics()!=null && transaction.getMosaics().size()!=0){
                    //代币
                    String mosaicIdString = getMosaicIdString(transaction.getMosaics().get(0));
                    xem.setMosaicIdString(mosaicIdString);
                    xem.setToken(true);
                    // 金额要区分是否是代币,,代币的除以比例可能不是100w,XEM 和 XPX 是100w
                    xem.setNumber(transaction.getAmount().divide(XEM_SCALE).multiply(transaction.getMosaics().get(0).getQuantity().divide(XEM_SCALE)));
                } else {
                    xem.setToken(false);
                    xem.setNumber(transaction.getAmount().divide(XEM_SCALE));
                }
                xem.setTo(transaction.getRecipient());
                xem.setFees(transaction.getFee().divide(XEM_SCALE));
                String payload = transaction.getMessage().getPayload();
                if (StringUtils.isNotBlank(payload)){
                    String memo = new String(HexEncoder.tryGetBytes(payload)).trim();
                    xem.setMemo(memo);
                }
                list.add(xem);
            } else if (transaction.getType()!=2049 && transaction.getOtherTrans()!=null){
                // 多重签名
                if (transaction.getOtherTrans().getMosaics()!=null && transaction.getOtherTrans().getMosaics().size()!=0){
                    //代币
                    String mosaicIdString = getMosaicIdString(transaction.getOtherTrans().getMosaics().get(0));
                    xem.setMosaicIdString(mosaicIdString);
                    xem.setToken(true);
                    xem.setNumber(transaction.getOtherTrans().getAmount().divide(XEM_SCALE).multiply(transaction.getOtherTrans().getMosaics().get(0).getQuantity().divide(XEM_SCALE)));
                } else {
                    xem.setToken(false);
                    xem.setFees(transaction.getOtherTrans().getFee().divide(XEM_SCALE));
                    xem.setNumber(transaction.getOtherTrans().getAmount().divide(XEM_SCALE));
                }
                xem.setTo(transaction.getOtherTrans().getRecipient());
                String payload = transaction.getOtherTrans().getMessage().getPayload();
                if (StringUtils.isNotBlank(payload)){
                    String memo = new String(HexEncoder.tryGetBytes(payload)).trim();
                    xem.setMemo(memo);
                }
                list.add(xem);
            }
        }
    }

    private static boolean isIntegerNumber(BigDecimal number){
        try {
            String s = number.toString();
            int index = s.indexOf(".");
            if (index == -1){
                return true;
            }
            String String = s.substring(index);
            if (BigDecimal.ZERO.compareTo(new BigDecimal("0"+String))!=0){
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String getMosaicIdString(Mosaics mosaic){
        String namespaceId = mosaic.getMosaicId().getNamespaceId();
        String name = mosaic.getMosaicId().getName();
        return namespaceId+":"+name;
    }

    private static String getMosaicIdString(Mosaic mosaic){
        String namespaceId = mosaic.getMosaicId().getNamespaceId().toString();
        String name = mosaic.getMosaicId().getName();
        return namespaceId+":"+name;
    }

    private static double getMinFee(double number,String memo,MosaicId mosaicId,boolean isXem){
        double fee = 0;
        if (memo!=null){
            int i = memo.getBytes().length / 32 + 1;
            fee = i * 0.05;
        }
        if (isXem){
            return fee+calculateXemTransferFee(number);
        }
        MosaicFeeInformation information = null;
        try {
            information = NamespaceMosaicsApi.findMosaicFeeInformationByNIS(mosaicId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        double maxMosaicQuantity = 9000000000000000D;
        double totalMosaicQuantity = information.getSupply().getRaw() * XEM_SCALE.doubleValue();
        double supplyRelatedAdjustment = Math.floor(0.8 * Math.log(maxMosaicQuantity / totalMosaicQuantity));
        double xemEquivalent = (8_999_999_999D * number * XEM_SCALE.doubleValue()) / totalMosaicQuantity;
        double xemFee = Math.floor(xemEquivalent / 10000);
        double unweightedFee = Math.min(Math.max(xemFee - supplyRelatedAdjustment,1),25) * 0.05;
        return new BigDecimal(fee + unweightedFee).setScale(2,RoundingMode.UP).doubleValue();
    }

    private static double calculateXemTransferFee(final double number) {
        return Math.min(Math.max(number/10000,1),25) * 0.05;
    }

    public static boolean checkAddress(String address) {
        if (!address.startsWith("N")) return false;
        return address.length() == 40;
    }


    //private static Amount calculateMinimumFee(long number,String memo,Mosaic mosaic,boolean isXem) {
    //    final long messageFee = null == memo
    //            ? 0
    //            : memo.getBytes().length / 32 + 1;
    //    if (isXem) {
    //        final long transferFee = calculateXemTransferFee(number);
    //        return Amount.fromNem(messageFee + transferFee);
    //    }
    //
    //    MosaicFeeInformation information = null;
    //    try {
    //        information = NamespaceMosaicsApi.findMosaicFeeInformationByNIS(mosaic.getMosaicId());
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    } catch (ExecutionException e) {
    //        e.printStackTrace();
    //    }
    //    if (null == information) {
    //        throw new IllegalArgumentException(String.format("unable to find fee information for '%s'", mosaic.getMosaicId()));
    //    }
    //
    //    final long transferFee = calculateMosaicTransferFee(Amount.fromNem(number), mosaic, information);
    //
    //    return Amount.fromMicroNem((messageFee + transferFee)*50000);
    //}
    //
    ///**
    // * Calculate mosaic transfer fee.
    // *
    // * @param amount the amount
    // * @param mosaic the mosaic
    // * @param information the information
    // * @return the long
    // */
    //private static long calculateMosaicTransferFee(
    //        final Amount amount,
    //        final Mosaic mosaic,
    //        final MosaicFeeInformation information) {
    //    if (0 == information.getDivisibility() && 10_000 >= information.getSupply().getRaw()) {
    //        return 1L;
    //    }
    //
    //    final long xemEquivalent = calculateXemEquivalent(amount, mosaic, information.getSupply(), information.getDivisibility());
    //    final long xemFee = calculateXemTransferFee(xemEquivalent);
    //    final long mosaicTotalQuantity = MosaicUtils.toQuantity(information.getSupply(), information.getDivisibility()).getRaw();
    //    final long supplyRelatedAdjustment = 0 < mosaicTotalQuantity
    //            ? (long)(0.8 * Math.log(MosaicConstants.MAX_QUANTITY / mosaicTotalQuantity))
    //            : 0;
    //    return Math.max(1L, xemFee - supplyRelatedAdjustment);
    //}
    //
    //private static long calculateXemEquivalent(final Amount amount, final Mosaic mosaic, final Supply supply, final int divisibility) {
    //    if (Supply.ZERO.equals(supply)) {
    //        return 0;
    //    }
    //
    //    return BigInteger.valueOf(MosaicConstants.MOSAIC_DEFINITION_XEM.getProperties().getInitialSupply())
    //            .multiply(BigInteger.valueOf(mosaic.getQuantity().getRaw()))
    //            .multiply(BigInteger.valueOf(amount.getNumMicroNem()))
    //            .divide(BigInteger.valueOf(supply.getRaw()))
    //            .divide(BigInteger.TEN.pow(divisibility + 6))
    //            .longValue();
    //}



}
