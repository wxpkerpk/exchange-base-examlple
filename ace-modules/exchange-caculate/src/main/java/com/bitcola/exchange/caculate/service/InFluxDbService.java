package com.bitcola.exchange.caculate.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.caculate.entity.Kline;
import com.bitcola.exchange.caculate.data.KlinesData;
import com.bitcola.exchange.caculate.data.PriceData;
import com.bitcola.exchange.caculate.data.TrendencyData;
import com.bitcola.exchange.caculate.data.VolData;
import lombok.extern.log4j.Log4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * @author:wx
 * @description:
 * @create:2018-08-10  23:33
 */
@Service
@Log4j
public class InFluxDbService {
    @Value(value = "${influx.host}")
    String host;
    @Value(value = "${influx.tablename}")
    String tableName;
    InfluxDB influxDB;

    static final long milliSeconds8h = 8 * 60 * 60 * 1000;

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }


    InfluxDBResultMapper resultMapper = new InfluxDBResultMapper(); // thread-safe - can be reused

    InfluxDB getInstance() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(host);
        }


        return influxDB;
    }

    public void initRetentionPolicy() {

        InfluxDB influxDB = getInstance();
        influxDB.createRetentionPolicy("forever", tableName, "365d", "24h", 2, false);
        influxDB.createRetentionPolicy("6d", tableName, "6d", "24h", 2, true);
        influxDB.createRetentionPolicy("30d", tableName, "30d", "24h", 2, true);
        influxDB.createRetentionPolicy("365d", tableName, "365d", "24h", 2, true);


    }

    public void insertValue(String code, long time, double price, double vol) {
        Point point = Point.measurement("kline")
                .time(time, TimeUnit.MILLISECONDS)
                .tag("code", code)
                .addField("price", price)
                .addField("vol", vol)
                .build();
        getInstance().write(tableName, "forever", point);
    }


    public Number[] queryLastKline(String pair, String type) {

        String queryStr = String.format("select FIRST(price),MAX(price),MIN(price),LAST(price),SUM(vol) from kline where code='%s'  and time > now() - %s  GROUP BY time(%s) ORDER BY time desc limit 1", pair, type, type);
        List<Number[]> klineList = getNumbers(queryStr);
        return klineList.size() > 0 ? klineList.get(0) : new Number[5];

    }

    @Cached(key = "#code + #start+#end + #type + #limit", cacheType = CacheType.LOCAL, expire = 4)
    public List<Number[]> queryKline(String code, Long start, Long end, String sample, int limit) {
        start = start * 1000000;
        end = end * 1000000;
        String queryStr = String.format("select FIRST(price),MAX(price),MIN(price),LAST(price),SUM(vol) from kline where code='%s'  and time > %d  and time < %d GROUP BY time(%s) limit %d", code, start, end, sample, limit);
        // String queryStr=String.format("select * from kline");
        List<Number[]> klineList = getNumbers(queryStr);


        return klineList;
    }

    private List<Number[]> getNumbers(String queryStr) {
        Query query = new Query(queryStr, tableName);
        QueryResult result = getInstance().query(query);
        List<KlinesData> klinesDataList = resultMapper.toPOJO(result, KlinesData.class);
        Kline kline = new Kline();
        return klinesDataList.stream().filter(it -> (it.getFirst() > 0)).map(x -> {
            try {
                if (!"".equals(x.getTime())) {
                    Date date = formatter.parse(x.getTime());
                    //加8小时
                    kline.setTime(date.getTime() + milliSeconds8h);
                } else {
                    kline.setTime(0);
                }
            } catch (ParseException e) {

                log.error(e);
                kline.setTime(0);

            }
            kline.setVol(x.getSum());
            kline.setLow(x.getMin());
            kline.setHigh(x.getMax());
            kline.setOpen(x.getFirst());
            kline.setClose(x.getLast());
            return kline.toArray();

        }).collect(Collectors.toList());
    }

    @Cached(key = " #code", cacheType = CacheType.LOCAL, expire = 6)

    public List<Number> getTrendency(String code) {
        String queryStr = String.format("select LAST(price) from kline where code='%s' and time > now() - 48h GROUP BY time(1h)", code);

        Query query = new Query(queryStr, tableName);
        QueryResult result = getInstance().query(query);
        List<TrendencyData> trendencyDataList = resultMapper.toPOJO(result, TrendencyData.class);

        return trendencyDataList.stream().map(TrendencyData::getLast).collect(Collectors.toList());

    }

    @Cached(key = "#code", cacheType = CacheType.LOCAL, expire = 2)

    public Double getMinIn24h(String code) {

        String queryStr = String.format("select MIN(price) as sum from kline where code='%s' and time >now() - 24h ", code);
        return getaDouble(queryStr);

    }

    @Cached(key = "#code", cacheType = CacheType.LOCAL, expire = 2)

    public Double getMaxIn24h(String code) {

        String queryStr = String.format("select MAX(price) as sum from kline where code='%s' and time >now() - 24h ", code);
        return getaDouble(queryStr);

    }

    private Double getaDouble(String queryStr) {
        Query query = new Query(queryStr, tableName);

        QueryResult result = getInstance().query(query);
        List<VolData> trendencyDataList = resultMapper.toPOJO(result, VolData.class);
        if (trendencyDataList.size() > 0) return trendencyDataList.get(0).getSum();
        return 0d;
    }

    @Cached(key = "#code", cacheType = CacheType.LOCAL, expire = 4)
    public double getVolIn24h(String code) {

        String queryStr = String.format("select sum(vol) from kline where code='%s' and time >now() - 24h ", code);
        return getaDouble(queryStr);
    }

    @Cached(key = "#code", cacheType = CacheType.LOCAL, expire = 2)

    public Double getPriceBefore24H(String code) {

        Date now = new Date();
        now.setHours(0);
        now.setMinutes(0);
        now.setSeconds(0);
        long time = now.getTime() * 1000000;
        String queryStr = String.format("select price from kline where code='%s' and time > %d limit 1", code, time);
        Query query = new Query(queryStr, tableName);
        QueryResult result = getInstance().query(query);
        List<PriceData> trendencyDataList = resultMapper.toPOJO(result, PriceData.class);
        if (trendencyDataList.size() > 0) return trendencyDataList.get(0).getPrice();
        return 0d;


    }


}
