package com.bitcola.activity.mapper;

import com.bitcola.activity.entity.SignUp;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;

@Repository
public interface SignUpMapper extends Mapper<SignUp> {
    BigDecimal total();

}
