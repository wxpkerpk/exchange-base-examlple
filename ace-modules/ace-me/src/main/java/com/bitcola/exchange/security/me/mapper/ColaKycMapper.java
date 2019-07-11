package com.bitcola.exchange.security.me.mapper;

import com.bitcola.me.entity.ColaUserKyc;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaKycMapper extends Mapper<ColaUserKyc> {
    Integer isDocumentNumberRepeat(ColaUserKyc kyc);
}
