package com.bitcola.exchange.security.admin.biz;

import com.bitcola.ctc.ColaCtcBankCard;
import com.bitcola.exchange.security.admin.util.BankCardUtil;
import com.bitcola.exchange.security.admin.mapper.ColaCtcBankCardMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.stereotype.Service;

/**
 * @author zkq
 * @create 2019-05-07 15:05
 **/
@Service
public class ColaCtcBankCardBiz extends BaseBiz<ColaCtcBankCardMapper, ColaCtcBankCard> {

    @Override
    public ColaCtcBankCard selectById(Object id){
        if (id == null) return null;
        ColaCtcBankCard bankCard = mapper.selectByPrimaryKey(id);
        if (!BankCardUtil.checkSign(bankCard.getSign(),bankCard.getCardId(),bankCard.getUserName(),bankCard.getUserId(),bankCard.getDocumentNumber())){
            throw new RuntimeException("银行卡签名出错:"+bankCard.getCardId());
        }
        return bankCard;
    }
}
