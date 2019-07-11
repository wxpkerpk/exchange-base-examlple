package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.common.constant.FinancialConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.TimeUtils;
import com.bitcola.exchange.security.me.dto.FinancialRecordsDto;
import com.bitcola.exchange.security.me.mapper.ColaFinancialRecordsMapper;
import com.bitcola.exchange.security.me.vo.FinancialRecordsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-10-24 19:27
 **/
@Service
public class ColaFinancialRecordsBiz {

    @Autowired
    ColaFinancialRecordsMapper mapper;

    public TableResultResponse list(FinancialRecordsDto dto) {
        dto.setUserId(BaseContextHandler.getUserID());
        List<FinancialRecordsVo> list = mapper.list(dto);
        Integer count = mapper.countList(dto);
        return new TableResultResponse(count,list);
    }

    public List<String> actionList(){
        List<String> list = new ArrayList<String>();
        list.add(FinancialConstant.DEPOSIT);
        list.add(FinancialConstant.WITHDRAW);
        list.add(FinancialConstant.SEND_RED_ENVELOPE);
        list.add(FinancialConstant.RECEIVE_RED_ENVELOPE);
        list.add(FinancialConstant.INVITE_REWARDS);
        list.add(FinancialConstant.DONATE_IN);
        list.add(FinancialConstant.DONATE_OUT);
        list.add(FinancialConstant.SYSTEM_REWARD);
        list.add(FinancialConstant.ACTIVITY_REWARD);
        return list;
    }

    public List<Object[]> csv(Integer excludeInviteRewards) {
        List<FinancialRecordsVo> list = mapper.cvs(BaseContextHandler.getUserID(),excludeInviteRewards);
        List<Object[]> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            FinancialRecordsVo vo = list.get(i);
            Object[] arr = new Object[]{i, TimeUtils.getDateTimeFormat(vo.getTime()),vo.getCoinCode(),
            vo.getActionType(),vo.getAccount(),vo.getStatus()};
            result.add(arr);
        }
        return result;
    }

    /**
     * 返回  地址, tx_id
     * @param id
     * @param action
     * @return
     */
    public Map<String, String> detail(String id, String action) {
        Map<String,String> detail = mapper.detail(id);
        String txid = detail.get("txid");
        if (StringUtils.isBlank(txid)){
            detail.put("txid",txid.split("@")[0]);
        }
        return detail;
    }

}
