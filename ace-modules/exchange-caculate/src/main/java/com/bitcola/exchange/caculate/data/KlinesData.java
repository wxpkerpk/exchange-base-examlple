package com.bitcola.exchange.caculate.data;

import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/*
 * @author:wx
 * @description:
 * @create:2018-08-29  23:47
 */
@Measurement(name = "kline")
@Data
public class KlinesData {
    @Column(name = "time")
    String time;
    @Column(name="first")
    double first;
    @Column(name="max")
    double max;
    @Column(name="min")
    double min;
    @Column(name="last")
    double last;
    @Column(name="sum")
    double sum;
}
