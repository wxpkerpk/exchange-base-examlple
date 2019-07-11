package com.bitcola.exchange.ctc.biz;


import com.bitcola.ctc.ColaCtcBankCard;
import com.bitcola.exchange.ctc.mapper.ColaCtcBankCardMapper;
import com.bitcola.exchange.ctc.util.BankCardVerify;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-05-07 15:05
 **/
@Service
@Transactional
public class ColaCtcBankCardBiz extends BaseBiz<ColaCtcBankCardMapper, ColaCtcBankCard> {

    /**
     * 此处加入签名验证,确保数据库保存的银行卡没有经过篡改
     * @param userID
     * @return
     */
    public List<ColaCtcBankCard> list(String userID) {
        List<ColaCtcBankCard> list = mapper.list(userID);
        for (ColaCtcBankCard bankCard : list) {
            if (!BankCardVerify.checkSign(bankCard.getSign(),bankCard.getCardId(),bankCard.getUserName(),bankCard.getUserId(),bankCard.getDocumentNumber())){
                throw new RuntimeException("银行卡签名出错:"+bankCard.getCardId());
            }
        }
        return list;
    }


    /**
     * 银行卡三要素认证
     * @return 返回错误的验证信息,如果验证成功,则返回 null
     */
    public String checkBankCard(String cardId, String realName, String documentNumber) {
        ColaCtcBankCard bankCard = mapper.selectByPrimaryKey(cardId);
        if (bankCard != null && bankCard.getChecked() == 1){
            if (!bankCard.getUserName().equals(realName) ){
                return "姓名输入错误";
            }
            if (!bankCard.getDocumentNumber().equals(documentNumber)){
                return "身份证号输入错误";
            } else {
                return null;
            }
        }
        return BankCardVerify.verify(cardId, documentNumber,realName );
    }

    public String add(ColaCtcBankCard bankCard) {
        ColaCtcBankCard card = mapper.selectByPrimaryKey(bankCard.getCardId());
        if (card == null){
            mapper.insertSelective(bankCard);
        } else if (StringUtils.isBlank(card.getUserId())){
            card.setChecked(1);
            card.setUserId(bankCard.getUserId());
            card.setBankName(bankCard.getBankName());
            card.setBankAddress(bankCard.getBankAddress());
            mapper.updateByPrimaryKeySelective(card);
        } else {
            return "该银行卡已经被绑定";
        }
        return null;
    }

    public List<Map<String, String>> bankList() {
        return mapper.bankList();
    }

    public Map<String, String> getBankInfo(String bankId) {
        return mapper.getBankInfo(bankId);
    }
}
