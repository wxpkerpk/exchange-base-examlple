package com.bitcola.activity.biz;

import com.bitcola.activity.mapper.SystemMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zkq
 * @create 2018-11-29 21:00
 **/
@Service
public class SystemBiz {

    @Autowired
    SystemMapper mapper;

}
