package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.me.entity.ColaUserKyc;
import com.bitcola.exchange.security.me.mapper.ColaKycMapper;
import org.springframework.stereotype.Service;

/**
 * @author zkq
 * @create 2018-12-06 14:44
 **/
@Service
public class ColaKycBiz extends BaseBiz<ColaKycMapper, ColaUserKyc> {
    public Integer isDocumentNumberRepeat(ColaUserKyc kyc) {
        return mapper.isDocumentNumberRepeat(kyc);
    }
}
