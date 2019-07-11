package com.bitcola.exchange.security.me.biz;


import com.alibaba.fastjson.JSONObject;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.ChainConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.MessageRequest;
import com.bitcola.exchange.security.me.constant.AuditStatusConstant;
import com.bitcola.exchange.security.me.feign.IConfigFeign;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.mapper.ColaIcoUserMapper;
import com.bitcola.me.entity.ColaIcoUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ico用户信息表
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-29 14:43:56
 */
@Service
public class ColaIcoUserBiz extends BaseBiz<ColaIcoUserMapper, ColaIcoUser> {

    @Autowired
    IPushFeign pushFeign;

    public static Map<String,String> addressMap = new HashMap<>();


    @Autowired
    StringRedisTemplate redisTemplate;

    public void audit(String id, String option) {
        ColaIcoUser colaIcoUser = mapper.selectByPrimaryKey(id);
        if (colaIcoUser.getCheckStatus() == 0){
            if (AuditStatusConstant.AGREE.equals(option)){
                colaIcoUser.setCheckStatus(1);
                //  调用 CTO 接口生成充值地址
                // redis 通知 topic
                MessageRequest request = new MessageRequest();
                String uuid = UUID.randomUUID().toString();
                request.setId(uuid);
                request.setTime(System.currentTimeMillis());
                request.setPath("btc/newAccount");
                redisTemplate.convertAndSend(ChainConstant.REQUEST_TOPIC,JSONObject.toJSONString(request));
                int i = 0;
                String address = null;
                while (address==null){
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    address = ColaIcoUserBiz.addressMap.get(uuid);
                    i++;
                    if (i == 15){
                        address = "";
                    }
                }

                if (StringUtils.isBlank(address)){
                    throw new RuntimeException("生成地址出错,请再次尝试");
                }
                colaIcoUser.setDepositAddress(address);
                addressMap.remove(uuid);

            } else if (AuditStatusConstant.REFUSE.equals(option)){
                //审核不通过
                colaIcoUser.setCheckStatus(-1);
            }
            mapper.updateByPrimaryKeySelective(colaIcoUser);
        }
    }


    @Autowired
    IConfigFeign configFeign;


    public Map<String,Object> getDeadlineAndSellPercent(){
        Map<String,Object> map =  new HashMap<>();
        long dead = Long.valueOf(getIcoConfig("ico_deadline")) - System.currentTimeMillis();
        if (dead < 0){
            dead = 0;
        }
        map.put("deadline",dead);
        BigDecimal number = this.colaTokenIcoTotalNumber();
        // ico + 天使轮 + 私募  / 总量
        BigDecimal percent = number.add(new BigDecimal(getIcoConfig("cola_ico_token_angel")))
                .add(new BigDecimal(getIcoConfig("cola_ico_token_private")))
                .divide(new BigDecimal(getIcoConfig("cola_token_number")),4,RoundingMode.HALF_UP);
        map.put("sellPercent",percent);
        return map;
    }

    public ColaIcoUser icoInfo() {
        return mapper.icoInfo(BaseContextHandler.getUserID());
    }

    public void userInfoSubmit(ColaIcoUser info) {
        info.setUserId(BaseContextHandler.getUserID());
        info.setId(UUID.randomUUID().toString());
        Integer status = icoStatus();
        if (status!=null && -1 == status){
            mapper.deleteIcoInfo(BaseContextHandler.getUserID());
        } else if (status!=null && 0 == status) {
            return ;
        } else if (status!=null && 1 == status) {
            return ;
        }
        info.setCheckStatus(0);
        pushFeign.sms("18380431467","86","18380431467");
        mapper.insertSelective(info);
    }

    public Integer icoStatus() {
       return mapper.icoStatus(BaseContextHandler.getUserID());
    }

    public BigDecimal colaTokenNumber() {
        return mapper.colaTokenNumber(BaseContextHandler.getUserID());
    }

    public Map<String, Object> colaTokenInfo() {
        Map<String,Object> map = new HashMap<>();
        map.put("price",pushFeign.btc());
        map.put("bonus",new BigDecimal(getIcoConfig("cola_token_bonus")));
        map.put("colaTokenPrice",new BigDecimal(getIcoConfig("cola_token_price")));
        return map;
    }

    public BigDecimal colaTokenIcoTotalNumber(){
        BigDecimal num = mapper.colaTokenIcoTotalNumber();
        return num == null? BigDecimal.ZERO:num;
    }


    public boolean checkAddress(String address) {
        Integer i = mapper.checkAddress(address,BaseContextHandler.getUserID());
        if (i > 0){
            return true;
        }
        return false;
    }

    @Cached(key = "getIcoConfig + #config", cacheType = CacheType.LOCAL, expire = 10)
    public String getIcoConfig(String config){
        return configFeign.getConfig(config);
    }


    public boolean checkSubscribeExist(String email) {
        Integer i = mapper.checkSubscribeExist(email);
        if (i > 0){
            return true;
        }
        return false;
    }

    public void subscribe(String email) {
        mapper.subscribe(UUID.randomUUID().toString(),email);
    }

}