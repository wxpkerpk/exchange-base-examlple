package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.me.mapper.ColaMeBalanceWithdrawAddressMapper;
import com.bitcola.me.entity.ColaMeBalanceWithdrawAddress;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 用户历史提现地址
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-04 17:20:06
 */
@Service
public class ColaMeBalanceWithdrawAddressBiz extends BaseBiz<ColaMeBalanceWithdrawAddressMapper,ColaMeBalanceWithdrawAddress> {
    public List<ColaMeBalanceWithdrawAddress> get(String coinCode) {
        return mapper.get(BaseContextHandler.getUserID(),coinCode);
    }

    public void add(String tag, String address, String note, String coinCode) {
        ColaMeBalanceWithdrawAddress addressTag = new ColaMeBalanceWithdrawAddress();
        addressTag.setId(UUID.randomUUID().toString());
        addressTag.setUserId(BaseContextHandler.getUserID());
        addressTag.setLabel(tag);
        addressTag.setAddress(address);
        addressTag.setNote(note);
        addressTag.setCoinCode(coinCode);
        addressTag.setTime(System.currentTimeMillis());
        List<ColaMeBalanceWithdrawAddress> list = this.get(coinCode);
        for (int i = 4; i < list.size(); i++) {
            mapper.deleteByPrimaryKey(list.get(i).getId());
        }
        mapper.insertSelective(addressTag);
    }
}