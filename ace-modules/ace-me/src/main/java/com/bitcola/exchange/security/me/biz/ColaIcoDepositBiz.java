package com.bitcola.exchange.security.me.biz;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.ChainConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.MessageRequest;
import com.bitcola.exchange.security.common.msg.RedisResponse;
import com.bitcola.exchange.security.me.feign.IConfigFeign;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.mapper.ColaIcoDepositMapper;
import com.bitcola.exchange.security.me.mapper.ColaIcoUserMapper;
import com.bitcola.exchange.security.me.vo.DepositVo;
import com.bitcola.me.entity.ColaIcoDeposit;
import com.bitcola.me.entity.ColaIcoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 充值详情表
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-29 14:43:56
 */
@Service
public class ColaIcoDepositBiz extends BaseBiz<ColaIcoDepositMapper, ColaIcoDeposit> {

    public static Map<String, RedisResponse> redisMap = new HashMap<>();

    private String temp = "";
    public void setTemp(String temp){
        this.temp = temp;
    }

    @Autowired
    IConfigFeign configFeign;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ColaIcoUserMapper icoUserMapper;

    @Autowired
    IPushFeign pushFeign;


    public List<DepositVo> depositList() {
        ColaIcoUser colaIcoUser = icoUserMapper.icoInfo(BaseContextHandler.getUserID());
        MessageRequest request = new MessageRequest();
        String uuid = UUID.randomUUID().toString();
        request.setId(uuid);
        request.setTime(System.currentTimeMillis());
        request.setPath("btc/getTransactions");
        Map map = new HashMap();
        map.put("address",colaIcoUser.getDepositAddress());
        request.setParams(map);
        redisTemplate.convertAndSend(ChainConstant.REQUEST_TOPIC, JSONObject.toJSONString(request));
        int j = 0;
        RedisResponse response = redisMap.get(uuid);
        while (response==null){
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                e.printStackTrace();
            }
            response = redisMap.get(uuid);
            j++;
            if (j == 15){
                response = new RedisResponse();
            }
        }

        // 处理
        try {
            String transactions = JSONObject.toJSONString(response.getData());
            JSONArray arr = JSONObject.parseObject(transactions).getJSONArray("transactions");
            for (int i = 0; i < arr.size(); i++) {
                JSONObject jsonObject = arr.getJSONObject(i);
                String txid = jsonObject.getString("txid");
                String address = jsonObject.getString("address");
                BigDecimal amount = jsonObject.getBigDecimal("amount");
                Integer confirmations = jsonObject.getInteger("confirmations");
                confirmations = confirmations<5?confirmations:6;
                insertDepositItem(txid,null,address,amount,confirmations);
            }
        } catch (Exception e){
            System.out.println("未收到交易信息,id: "+uuid);
        }
        redisMap.remove(uuid);

        List<DepositVo> list = new ArrayList<>();
        List<ColaIcoDeposit> colaIcoDeposits = mapper.depositList(BaseContextHandler.getUserID());
        for (ColaIcoDeposit colaIcoDeposit : colaIcoDeposits) {
            DepositVo vo = new DepositVo();
            vo.setAmount(colaIcoDeposit.getDepositNumber());
            vo.setTimestamp(colaIcoDeposit.getTime());
            vo.setDepositStatus(colaIcoDeposit.getConfirmStatus());
            vo.setConfirmationCount(colaIcoDeposit.getCurrentConfirmNumber()+"/"+colaIcoDeposit.getConfirmNumber());
            list.add(vo);
        }
        return list;
    }



    public void updateDepositById(Integer currentConfirmNumber, String id, String status){
        mapper.updateDepositById(currentConfirmNumber,id,status);
    }

    public void insertDepositItem(String id, String fromAddress, String toAddress, BigDecimal number, Integer currentConfirmNumber){
        // 验证 id 是否存在
        if (mapper.existsWithPrimaryKey(id)){
            String status = "Pending";
            if (currentConfirmNumber == 6){
                status = "Complete";
            }
            updateDepositById(currentConfirmNumber,id,status);
            return;
        }


        BigDecimal price = pushFeign.btc();
        ColaIcoDeposit deposit = new ColaIcoDeposit();
        deposit.setId(id);
        deposit.setUserId(getUserIdByAddress(toAddress));
        deposit.setPrice(price);
        deposit.setDepositNumber(number);
        // token数量 =  eth * ethPrice / 兑换比例
        BigDecimal colaTokenNumber = number.multiply(price).divide(new BigDecimal(configFeign.getConfig("cola_token_price")),2, RoundingMode.HALF_UP);
        // 奖励数量 = token 数量 * 奖励百分比
        BigDecimal bonusNumber = colaTokenNumber.multiply(new BigDecimal(configFeign.getConfig("cola_token_bonus")));
        deposit.setColaTokenNumber(colaTokenNumber);
        deposit.setBonusNumber(bonusNumber);
        deposit.setTime(System.currentTimeMillis());
        deposit.setConfirmStatus("Pending");
        deposit.setConfirmNumber(6);
        deposit.setCurrentConfirmNumber(currentConfirmNumber);
        deposit.setGrantNumber(BigDecimal.ZERO);
        deposit.setNotGrantNumber(colaTokenNumber.add(bonusNumber));
        mapper.insertDepositItem(deposit);
    }

    public String getUserIdByAddress(String address){
        return mapper.getUserIdByAddress(address);
    }

}