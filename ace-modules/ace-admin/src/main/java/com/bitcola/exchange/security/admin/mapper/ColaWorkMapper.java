package com.bitcola.exchange.security.admin.mapper;

import org.springframework.stereotype.Repository;

@Repository
public interface ColaWorkMapper {
    Long countWorkOrder();

    Long countWithdraw();

    Long countKyc();

}
