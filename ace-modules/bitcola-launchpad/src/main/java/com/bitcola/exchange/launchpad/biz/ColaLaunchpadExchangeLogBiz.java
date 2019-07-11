package com.bitcola.exchange.launchpad.biz;

import com.bitcola.exchange.launchpad.constant.ExchangeLogStatus;
import com.bitcola.exchange.launchpad.dto.ColaUserBalance;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadExchangeLog;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadProjectIeo;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadExchangeLogMapper;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadProjectIeoMapper;
import com.bitcola.exchange.launchpad.vo.CoinResult;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-03-15 12:34
 **/
@Service
public class ColaLaunchpadExchangeLogBiz extends BaseBiz<ColaLaunchpadExchangeLogMapper,ColaLaunchpadExchangeLog> {

    @Autowired
    ColaLaunchpadProjectIeoMapper ieoMapper;

    public List<ColaLaunchpadExchangeLog> list(AdminQuery query){
        return mapper.list(query);
    }


    public Long total(AdminQuery query) {
        return mapper.total(query);
    }

    @Transactional
    public Map<String,Object> issue(String projectId, Boolean containProject, BigDecimal projectRate) {
        ColaLaunchpadProjectIeo ieo = ieoMapper.selectProjectById(projectId);
        if (ieo == null || ieo.getStatus() != 2) return null;
        List<ColaLaunchpadExchangeLog> list = mapper.listIssue(projectId);
        Map<String, ColaUserBalance> map = new HashMap<>();
        Map<String, ColaUserBalance> projectBalanceMap = new HashMap<>();
        BigDecimal userTotal = BigDecimal.ZERO;
        for (ColaLaunchpadExchangeLog log : list) {
            BigDecimal number = log.getNumber().add(log.getReward());
            String coinCode = log.getCoinCode();
            String userId = log.getUserId();
            ColaUserBalance balance = map.computeIfAbsent(userId + coinCode, k -> new ColaUserBalance(userId + coinCode));
            balance.setAvailable(balance.getAvailable().add(number));
            ColaUserBalance adminBalance = map.computeIfAbsent(UserConstant.SYS_ADMIN + coinCode, k -> new ColaUserBalance(UserConstant.SYS_ADMIN + coinCode));
            adminBalance.setFrozen(adminBalance.getFrozen().subtract(number));
            userTotal = userTotal.add(number);
            // 项目方资金计算
            if (projectRate == null) projectRate = BigDecimal.ZERO;
            BigDecimal bitcolaNumber = log.getPrice().multiply(log.getNumber()).multiply(projectRate);
            BigDecimal projectNumber = log.getPrice().multiply(log.getNumber()).subtract(bitcolaNumber);
            String projectBalanceId = ieo.getUserId() + log.getSymbol();
            ColaUserBalance projectBalance = projectBalanceMap.computeIfAbsent(projectBalanceId, k -> new ColaUserBalance(projectBalanceId));
            projectBalance.setAvailable(projectBalance.getAvailable().add(projectNumber));
            String bitcolaBalanceId = UserConstant.SYS_ACCOUNT_ID + log.getSymbol();
            ColaUserBalance bitcolaBalance = projectBalanceMap.computeIfAbsent(bitcolaBalanceId, k -> new ColaUserBalance(bitcolaBalanceId));
            bitcolaBalance.setAvailable(bitcolaBalance.getAvailable().add(bitcolaNumber));
            ColaUserBalance adminBalanceSymbol = map.computeIfAbsent(UserConstant.SYS_ADMIN + log.getSymbol(),
                    k -> new ColaUserBalance(UserConstant.SYS_ADMIN + log.getSymbol()));
            adminBalanceSymbol.setFrozen(adminBalanceSymbol.getFrozen().subtract(log.getPrice().multiply(log.getNumber())));


        }
        // 没有卖完的解冻到项目方的可用里面
        BigDecimal remain = ieo.getNumber().subtract(userTotal);
        if (remain.compareTo(BigDecimal.ZERO)>0){
            ColaUserBalance balance2 = map.computeIfAbsent(ieo.getUserId() + ieo.getCoinCode(), k -> new ColaUserBalance(ieo.getUserId() + ieo.getCoinCode()));
            balance2.setFrozen(balance2.getFrozen().subtract(remain));
            balance2.setAvailable(balance2.getAvailable().add(remain));
        }

        List<ColaUserBalance> balanceList = new ArrayList<>();
        for (String key : map.keySet()) {
            balanceList.add(map.get(key));
        }

        List<CoinResult> project = new ArrayList<>();
        for (String key : projectBalanceMap.keySet()) {
            ColaUserBalance balance = projectBalanceMap.get(key);
            project.add(new CoinResult(balance.getId(),balance.getAvailable().add(balance.getFrozen())));
            if (containProject!=null && containProject){
                balanceList.add(balance);
            }
        }
        if (balanceList.size()>0){
            ieoMapper.batchUpdateUserBalance(balanceList, EncoderUtil.BALANCE_KEY);
        }
        mapper.updateStatus(projectId, ExchangeLogStatus.ISSUED);
        ieoMapper.updateProjectStatus(ieo.getId(),3);
        Map<String,Object> result = new HashMap<>();
        result.put("user",new CoinResult(ieo.getCoinCode(),userTotal));
        result.put("project",project);
        return result;
    }
}
