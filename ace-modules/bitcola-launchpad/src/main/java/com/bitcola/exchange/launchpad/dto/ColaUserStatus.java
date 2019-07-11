package com.bitcola.exchange.launchpad.dto;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-03-15 18:19
 **/
@Data
public class ColaUserStatus {
    String id;
    String pin;
    Integer kyc = 0;
}
