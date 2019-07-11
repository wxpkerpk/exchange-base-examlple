package com.bitcola.chain.chain.xem.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-11-23 19:18
 **/
@Data
public class Mosaics {
    MosaicId mosaicId;
    BigDecimal quantity;
}
