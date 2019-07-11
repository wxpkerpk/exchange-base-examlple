package com.bitcola.exchange.util;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.data.*;
import com.bitcola.exchange.security.common.util.TimeUtils;
import lombok.extern.log4j.Log4j2;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * @author:wx
 * @description:
 * @create:2018-08-10  23:33
 */
@Service
@Log4j2
public class InFluxDbService {
    @Value(value = "${influx.host}")
    String host;
    @Value(value = "${influx.database}")
    String database;
    InfluxDB influxDB;


    InfluxDBResultMapper resultMapper = new InfluxDBResultMapper(); // thread-safe - can be reused

    InfluxDB getInstance() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(host);
        }


        return influxDB;
    }

    public void initRetentionPolicy() {

        InfluxDB influxDB = getInstance();
        influxDB.createRetentionPolicy("forever", database, "365d", "24h", 2, false);
        influxDB.createRetentionPolicy("6d", database, "6d", "24h", 2, true);
        influxDB.createRetentionPolicy("30d", database, "30d", "24h", 2, true);
        influxDB.createRetentionPolicy("365d", database, "365d", "24h", 2, true);


    }

    public void insertValue(String code, Kline kline) {
        Point point = Point.measurement("kline_new")
                .time(kline.getTime(), TimeUnit.MILLISECONDS)
                .tag("code", code)
                .addField("open", kline.getOpen())
                .addField("high", kline.getHigh())
                .addField("low", kline.getLow())
                .addField("close", kline.getClose())
                .addField("vol", kline.getVol())
                .build();
        getInstance().write(database, "forever", point);
    }


    @Cached(name = "kline",cacheType = CacheType.LOCAL, expire = 30)
    public List<Number[]> queryKline(String code, Long start, Long end, String sample, int limit) {
        start = start * 1000000;
        end = end * 1000000;
        String queryStr = String.format("select FIRST(open),MAX(high),MIN(low),LAST(close),SUM(vol) from kline_new where code='%s' and time >= %d and time < %d GROUP BY time(%s) order by time desc limit %d", code, start,end, sample, limit);
        return getNumbers(queryStr);
    }

    public static void main(String[] args) {
        InFluxDbService service = new InFluxDbService();
        service.database = "bitcola";
        service.host = "http://120.79.250.164:8086";
        List<Number[]> eos_usdt = service.queryKline("ETH_USDT", 1551667284811L, 1552387284811L, "15m", 20);
        for (Number[] numbers : eos_usdt) {
            System.out.println(TimeUtils.getDateTimeFormat(numbers[0].longValue()) + Arrays.asList(numbers));
        }

    }

    private List<Number[]> getNumbers(String queryStr) {
        List<Number[]> list = new ArrayList<>();
        Query query = new Query(queryStr, database);
        QueryResult result = getInstance().query(query);
        List<KlinesData> klinesDataList = resultMapper.toPOJO(result, KlinesData.class);
        KlinesData lastData = null;
        for (KlinesData data : klinesDataList) {
            Kline kline = new Kline();
            if (data.getFirst() > 0){
                lastData = data;
            }
            if (lastData != null && data.getFirst() <= 0){
                kline.setTime(data.getTime().toEpochMilli());
                kline.setOpen(new BigDecimal(lastData.getLast()));
                kline.setHigh(new BigDecimal(lastData.getLast()));
                kline.setClose(new BigDecimal(lastData.getLast()));
                kline.setLow(new BigDecimal(lastData.getLast()));
                list.add(kline.toArray());
            } else if (lastData != null){
                kline.setTime(data.getTime().toEpochMilli());
                kline.setOpen(new BigDecimal(data.getFirst()));
                kline.setHigh(new BigDecimal(data.getMax()));
                kline.setClose(new BigDecimal(data.getLast()));
                kline.setLow(new BigDecimal(data.getMin()));
                kline.setVol(new BigDecimal(data.getSum()));
                list.add(kline.toArray());
            }
        }
        return list;
    }


    @Cached(cacheType = CacheType.LOCAL, expire = 30,timeUnit = TimeUnit.MINUTES)
    public List<Number> getTendency(String code) {
        String queryStr = String.format("select LAST(close) from kline_new where code='%s' and time > now() - 48h GROUP BY time(1h)", code);
        Query query = new Query(queryStr, database);
        QueryResult result = getInstance().query(query);
        List<TrendencyData> tendencyDataList = resultMapper.toPOJO(result, TrendencyData.class);
        List<Number> list = new ArrayList<>();
        double lastData = 0;
        for (TrendencyData data : tendencyDataList) {
            if (data.getLast() != null){
                lastData = data.getLast();
            }
            list.add(lastData);
        }
        return list;

    }

    /**
     * 当前价格
     * @param pair
     * @return
     */
    public BigDecimal getMarketPrice(String pair){
        String queryStr = String.format("select close from kline_new where code='%s' order by time desc limit 1",pair);
        Query query = new Query(queryStr, database);
        QueryResult result = getInstance().query(query);
        List<PriceData> marketPrice = resultMapper.toPOJO(result, PriceData.class);
        if (marketPrice.size() > 0) return new BigDecimal(marketPrice.get(0).getClose());
        return BigDecimal.ZERO;
    }
    /**
     * 昨日价格
     * @param pair
     * @return
     */
    public BigDecimal getYesterdayPrice(String pair){
        String queryStr = String.format("select last(close) from kline_new where code='%s' and time > now() - 1d group by time(1d) order by time asc limit 1",pair);
        Query query = new Query(queryStr, database);
        QueryResult result = getInstance().query(query);
        List<TrendencyData> marketPrice = resultMapper.toPOJO(result, TrendencyData.class);
        if (marketPrice == null) return BigDecimal.ZERO;
        if (marketPrice.size() > 0) {
            TrendencyData data = marketPrice.get(0);
            if (data == null || data.getLast() == null) return BigDecimal.ZERO;
            return new BigDecimal(data.getLast());
        }
        return BigDecimal.ZERO;
    }


    public BigDecimal getVol24H(String pair) {
        String queryStr = String.format(" select sum(vol) from kline_new where code='%s' and time > now() - 1d group by time(1d) order by time desc limit 1",pair);
        Query query = new Query(queryStr, database);
        QueryResult result = getInstance().query(query);
        List<VolData> marketPrice = resultMapper.toPOJO(result, VolData.class);
        if (marketPrice.size() > 0) return new BigDecimal(marketPrice.get(0).getSum());
        return BigDecimal.ZERO;
    }

    public BigDecimal getYesterdayVol(String pair) {
        String queryStr = String.format("select sum(vol) from kline_new where code='%s' and time > now() - 2d group by time(1d) order by time desc limit 2",pair);
        Query query = new Query(queryStr, database);
        QueryResult result = getInstance().query(query);
        List<VolData> marketPrice = resultMapper.toPOJO(result, VolData.class);
        if (marketPrice == null) return BigDecimal.ZERO;
        if (marketPrice.size() > 1) {
            VolData data = marketPrice.get(1);
            if (data == null || data.getSum() == null) return BigDecimal.ZERO;
            return new BigDecimal(data.getSum());
        }
        return BigDecimal.ZERO;
    }
}
