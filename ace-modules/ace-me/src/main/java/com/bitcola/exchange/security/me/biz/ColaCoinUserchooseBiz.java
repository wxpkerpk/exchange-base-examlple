package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.me.entity.ColaCoinUserchoose;
import com.bitcola.exchange.security.me.mapper.ColaCoinUserchooseMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.bitcola.exchange.security.common.biz.BaseBiz;

import java.util.List;

/**
 * 用户自选表
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Service
public class ColaCoinUserchooseBiz extends BaseBiz<ColaCoinUserchooseMapper,ColaCoinUserchoose> {
    public int isExist(String coinCode, String symbol) {
        return mapper.isExist(coinCode,symbol, BaseContextHandler.getUserID());
    }

    public void remove(ColaCoinUserchoose userchoose) {
        if (StringUtils.isBlank(userchoose.getId())){
            if (StringUtils.isNotBlank(userchoose.getPair())){
                String[] s = userchoose.getPair().split("_");
                mapper.removeByCoinCode(s[0],s[1],BaseContextHandler.getUserID());
            } else {
                mapper.removeByCoinCode(userchoose.getCoinCode(),userchoose.getSymbol(),BaseContextHandler.getUserID());

            }
        } else {
            mapper.removeById(userchoose.getId());
        }
    }

    public List<ColaCoinUserchoose> list() {
        return mapper.list(BaseContextHandler.getUserID());
    }
}