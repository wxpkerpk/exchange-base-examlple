package com.bitcola.exchange.caculate.data;

import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/*
 * @author:wx
 * @description:
 * @create:2018-09-05  19:06
 */

@Measurement(name = "kline")
@Data
public class VolData {
    @Column(name="sum")
    Double sum;
}


