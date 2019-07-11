package com.bitcola.exchange.ctc.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-05-07 17:49
 **/
@Data
public class ColaUser {
    String userId;
    Integer kycStatus;
    String documentNumber;
    String firstName;
    String lastName;
    String pin;
}
