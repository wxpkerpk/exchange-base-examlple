package com.bitcola.exchange.security.admin.biz;

import com.bitcola.ctc.ColaCtcBankCard;
import com.bitcola.exchange.security.admin.util.BankCardUtil;
import com.bitcola.exchange.security.admin.entity.ColaCtcBusinessCard;
import com.bitcola.exchange.security.admin.mapper.ColaCtcBankCardMapper;
import com.bitcola.exchange.security.admin.mapper.ColaCtcBusinessCardMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.UserConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-07 15:05
 **/
@Service
@Transactional
public class ColaCtcBusinessCardBiz extends BaseBiz<ColaCtcBusinessCardMapper,ColaCtcBusinessCard> {

    @Autowired
    ColaCtcBankCardMapper bankCardMapper;

    public void insert(ColaCtcBankCard bankCard) {
        bankCard.setUserId(UserConstant.SYS_CTC_ID);
        bankCard.setSign(BankCardUtil.sign( bankCard.getCardId() ,bankCard.getUserName() ,bankCard.getUserId(), bankCard.getDocumentNumber()));
        bankCardMapper.insertSelective(bankCard);
        ColaCtcBusinessCard businessCard = new ColaCtcBusinessCard();
        businessCard.setCardId(bankCard.getCardId());
        businessCard.setBalance(BigDecimal.ZERO);
        businessCard.setAvailable(1);
        mapper.insert(businessCard);
    }
}
