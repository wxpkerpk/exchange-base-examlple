package com.bitcola.dataservice.biz;

import com.bitcola.caculate.entity.*;
import com.bitcola.caculate.entity.Vo.Payback;
import com.bitcola.caculate.entity.Vo.TransForms;
import com.bitcola.config.DataServiceConstant;
import com.bitcola.dataservice.exception.LACK_BALANCE_EXCEPTION;
import com.bitcola.dataservice.mapper.ColaCaculateExchangeLogMapper;
import com.bitcola.dataservice.mapper.ColaCaculaterOrderMapper;
import com.bitcola.dataservice.mapper.ColaUserBalanceMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.OrderStateConstants;
import com.bitcola.me.entity.ColaMeBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 用户资金
 *
 * @author zkq
 * @create 2018-07-14 14:18
 **/
@Service
public class ColaOrderBiz extends BaseBiz<ColaCaculaterOrderMapper, ColaOrder> {

    @Autowired
    ColaCaculaterOrderMapper mapper;
    @Autowired
    ColaUserBalanceMapper userBalanceMapper;
    @Autowired
    ColaUserBalanceBiz colaUserBalanceBiz;
    @Autowired
    ColaUserBalanceMapper colaUserBalanceMapper;
    @Autowired
    ColaCaculateExchangeLogMapper colaCaculateExchangeLogMapper;

    @Transactional
    public void matchOrder(List<String>completed, List<TransForms> transForms, List<Payback> paybacks, List<ColaOrder> unCompletedOrder, List<ExchangeLog>exchangeLogs) throws Exception {
        colaCaculateExchangeLogMapper.insertBatch(exchangeLogs);
        if(unCompletedOrder!=null&&unCompletedOrder.size()>0) mapper.updateUnCompletedOrder(unCompletedOrder);
        int tag = mapper.updateCompletedOrder(completed);
        if (tag != completed.size()) throw new Exception("状态不一致");
        for(TransForms t:transForms){
            colaUserBalanceBiz.transformBalance(t.getUserId(),t.getFromCode(),t.getToCode(),t.getFrom(),t.getTo());
        }
        for(Payback p:paybacks){
            colaUserBalanceMapper.reduceUserFrozenBanlance(p.getUserId(),p.getCoinCode(),p.getCount(),EncoderUtil.BALANCE_KEY);
            colaUserBalanceMapper.addUserBanlance(p.getUserId(),p.getCoinCode(),p.getCount(),EncoderUtil.BALANCE_KEY);
        }
    }

    @Transactional
    public DepthData selectDepth(String code, int limit, double precision, double minCountPrecision,long time) {
        DepthData depthData = new DepthData();
        List<DepthLine> depthSell = mapper.selectSellDepth(code, limit, precision, minCountPrecision,time);
        List<DepthLine> depthBuy = mapper.selectBuyDepth(code, limit, precision, minCountPrecision,time);


        if (depthSell.size() > 0) depthData.ask = new Number[depthSell.size()][2];//卖
        if (depthBuy.size() > 0) depthData.bids = new Number[depthBuy.size()][2];//买

        for (int i = 0; i < depthBuy.size(); i++) {
            depthData.bids[i] = new Number[]{depthBuy.get(i).getPrice(), depthBuy.get(i).getCountSum()};
        }

        for (int i = 0; i < depthSell.size(); i++) {
            depthData.ask[i] = new Number[]{depthSell.get(i).getPrice(), depthSell.get(i).getCountSum()};
        }
        return depthData;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void cancelOrder(String id) throws LACK_BALANCE_EXCEPTION {

        ColaOrder colaOrder = mapper.selectById(id);
        if (colaOrder.getStatus().equals(OrderStateConstants.Pending)) {

            String code = null;
            String codes[] = colaOrder.getCoinCode().split("_");
            BigDecimal count = BigDecimal.ZERO;
            switch (colaOrder.getType()) {
                case "buy": {
                    code = codes[1];
                    count = colaOrder.getTotal();
                    break;

                }
                case "sell": {
                    code = codes[0];
                    count = colaOrder.getCount();
                    break;
                }


            }

            CoinChange coinChange = new CoinChange();
            coinChange.setCoinCode(code);
            coinChange.setGain(count);
            coinChange.setUserID(colaOrder.getUserId());
            coinChange.setType(DataServiceConstant.OPERATION.REDUCE_FROZEN);

            colaUserBalanceBiz.forceReduceFrozenBalance(colaOrder.getUserId(),code,count);
            coinChange.setType(DataServiceConstant.OPERATION.ADD_BANLANCE);
            userBalanceMapper.addUserBanlance(colaOrder.getUserId(),code,count, EncoderUtil.BALANCE_KEY);
            mapper.updateOrderState(OrderStateConstants.Canceled, id);
        }


    }


    public List<ColaOrder> search(String userId, String code, String state, int start, int size, String type, Long startTime, Long endTime, String pairL, String pairR) {
        return mapper.selectUserAndCode(userId, code, state, start, size, type, startTime, endTime, pairL, pairR);
    }

    @Transactional
    public ColaOrder findById(String id) {
        return mapper.selectById(id);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int reduceCount(Collection<ColaOrder> coinChanges) throws Exception {
        Integer tag = 0;

        for (ColaOrder coinChange : coinChanges) {
            tag = mapper.reduceCount(coinChange);
            if (tag == 0) throw new Exception("数量不足");
        }


        return 0;
    }

    public Long countSelfOrders(String userId, String code, String state, String type, Long startTime, Long endTime, String pairL, String pairR) {
        return mapper.countSelfOrders(userId, code, state, type, startTime, endTime, pairL, pairR);
    }

    @Transactional
    public List<Map<String, Object>> orderManagement(String userId, String code, String state, Integer page, Integer size, String type, Long startTime, Long endTime, String pairL, String pairR) {
        List<Map<String, Object>> list = mapper.orderManagement(userId, code, state, page, size, type, startTime, endTime, pairL, pairR);
        return list;
    }

    public Long countOrderManagement(String userId, String code, String state, String type, Long startTime, Long endTime, String pairL, String pairR) {
        return mapper.countOrderManagement(userId, code, state, type, startTime, endTime, pairL, pairR);
    }


    void removeMap(Map map) {
        map.remove("to_order_id");
        map.remove("from_order_id");
        map.remove("from_fee");
        map.remove("to_fee");
        map.remove("from_count");
        map.remove("to_count");


    }

    public List<Map<String, Object>> orderHistory(String userId, Long timestamp, String code, String type, Integer size, Integer isPending) {
        List<Map<String, Object>> maps = mapper.orderHistory(userId, timestamp, code, type, size, isPending);
        if (maps.size() > 0) {
            List<String> ids = maps.stream().map(x -> x.get("id").toString()).collect(Collectors.toList());

            //查询成交记录
            List<Map<String, Object>> details = mapper.orderHistoryDetail(ids);
            var idMapper = new HashMap<String, List<Map<String, Object>>>();
            details.forEach(d -> {
                        Map<String, Object> buyMap = new HashMap<>(d);
                        Map<String, Object> sellMap = new HashMap<>(d);

                        String buyId = d.get("to_order_id").toString();
                        buyMap.put("orderid", buyId);
                        buyMap.put("fee", d.get("to_fee"));
                        buyMap.put("count", d.get("from_count"));
                        String sellId = d.get("from_order_id").toString();
                        sellMap.put("orderid", sellId);
                        sellMap.put("fee", d.get("from_fee"));
                        sellMap.put("count", d.get("from_count"));
                        removeMap(sellMap);
                        removeMap(buyMap);
                        putMapper(idMapper, buyMap, buyId);
                        putMapper(idMapper, sellMap, sellId);
                    }
            );
            for (Map<String, Object> map : maps) {
                String id = map.get("id").toString();
                List<Map<String, Object>> m = idMapper.get(id);
                if (m == null){
                    m = new ArrayList<>();
                }
                map.put("records",m );
            }

        }
        return maps;
    }

    private static void putMapper(HashMap<String, List<Map<String, Object>>> idMapper, Map<String, Object> buyMap, String buyId) {
        if (buyId != null) {
            var list = idMapper.get(buyId);
            if (list == null) list = new ArrayList<>(8);
            list.add(buyMap);
            idMapper.put(buyId, list);
        }
    }
}
