package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.mapper.ColaWorkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-01-31 17:43
 **/
@Service
public class ColaWorkBiz {

    @Autowired
    ColaWorkMapper mapper;

    public Map<String, Object> overview() {
        Map<String, Object> map = new HashMap<>();
        Long workOrder = mapper.countWorkOrder();
        Long withdraw = mapper.countWithdraw();
        Long kyc = mapper.countKyc();
        map.put("workOrder",workOrder);
        map.put("withdraw",withdraw);
        map.put("kyc",kyc);
        return map;
    }
}
