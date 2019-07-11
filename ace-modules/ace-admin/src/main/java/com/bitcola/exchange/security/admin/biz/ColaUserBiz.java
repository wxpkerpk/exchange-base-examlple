package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.entity.SysUserEntity;
import com.bitcola.exchange.security.admin.feign.IActivityFeign;
import com.bitcola.exchange.security.admin.mapper.ColaKycMapper;
import com.bitcola.exchange.security.admin.mapper.ColaUserMapper;
import com.bitcola.exchange.security.admin.vo.UserAddress;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.exchange.security.common.util.Query;
import com.bitcola.me.entity.ColaUserKyc;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.lang.reflect.ParameterizedType;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author zkq
 * @create 2018-11-27 12:06
 **/
@Service
public class ColaUserBiz {
    @Autowired
    ColaUserMapper mapper;

    @Autowired
    ColaKycMapper kycMapper;

    @Autowired
    IActivityFeign activityFeign;

    public TableResultResponse<SysUserEntity> selectByQuery(AdminQuery query) {
        List<SysUserEntity> list = mapper.list(query);
        Long total = mapper.total(query);
        return new TableResultResponse<>(total,list);
    }

    public Map<String, Object> one(String id) {
        Map<String, Object> info = mapper.info(id);
        List<Map<String, Object>> balance = mapper.balance(id);
        List<Map<String, Object>> transaction = mapper.transaction(id);
        Map<String, Object> map = new HashMap<>();
        map.put("info",info);
        map.put("balance",balance);
        map.put("transaction",transaction);
        return map;
    }

    public TableResultResponse<ColaUserKyc> kycList(Query query) {
        Example example = new Example(ColaUserKyc.class);
        if(query.entrySet().size()>0) {
            Example.Criteria criteria = example.createCriteria();
            for (Map.Entry<String, Object> entry : query.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("kycStatus")){
                    criteria.andEqualTo(entry.getKey(),Integer.valueOf(entry.getValue().toString()));
                } else {
                    criteria.andLike(entry.getKey(), "%" + entry.getValue().toString() + "%");
                }
            }
        }
        example.orderBy("timestamp").desc();
        Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
        List<ColaUserKyc> list = kycMapper.selectByExample(example);
        return new TableResultResponse<>(result.getTotal(), list);
    }

    public void auditKyc(String userId, Integer status, String reason) {
        ColaUserKyc colaUserKyc = kycMapper.selectByPrimaryKey(userId);
        colaUserKyc.setKycStatus(status);
        colaUserKyc.setReason(reason);
        kycMapper.updateByPrimaryKeySelective(colaUserKyc);
        // 审核通过,发奖励
        if (status == 1){
            activityFeign.kycReward(userId);
        }
    }

    public Map<String, Object> kycDetail(String userId) {
        return kycMapper.kycDetail(userId);
    }

    public void update(String id, String username, String pin, Integer enable, String email, String telephone, String areaCode) {
        if (StringUtils.isNotBlank(username)||StringUtils.isNotBlank(email)||StringUtils.isNotBlank(telephone)||StringUtils.isNotBlank(areaCode) ){
            mapper.updateBaseUser(id,username,email,telephone,areaCode);
        }
        if ("0".equals(pin)){
            mapper.resetPin(id);
        }
        if (enable != null){
            mapper.updateColaUser(id, enable);
        }
    }

    public Integer repeat(String username, String email, String areaCode, String telephone) {
        return mapper.repeat(username,email,areaCode,telephone);
    }

    public TableResultResponse inviterList(AdminQuery query) {
        List<SysUserEntity> list = mapper.inviterList(query);
        Long total = mapper.inviterCount(query);
        return new TableResultResponse<>(total,list);
    }

    public TableResultResponse userAddressList(AdminQuery query) {
        List<UserAddress> list = mapper.userAddressList(query);
        for (UserAddress userAddress : list) {
            userAddress.setAvailable(userAddress.getAvailable().setScale(5, RoundingMode.HALF_UP));
            userAddress.setFrozen(userAddress.getFrozen().setScale(5, RoundingMode.HALF_UP));
        }
        Long total = mapper.userAddressCount(query);
        return new TableResultResponse<>(total,list);
    }
}
