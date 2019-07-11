package com.bitcola.exchange.caculate.dataservice;

import com.bitcola.caculate.entity.CaculateParams;
import com.bitcola.caculate.entity.CoinChange;
import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.caculate.entity.Vo.VoCaculateParams;
import com.bitcola.exchange.security.common.msg.ObjectRestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FeignClient("dataservice")
public interface ColaBalanceService {
    @RequestMapping(value = "/balance/changeBalance",method = RequestMethod.POST)
     ObjectRestResponse changeBalance(@RequestBody ArrayList<CoinChange> coinChanges);




    @RequestMapping(value = "/balance/makeOrder", method = RequestMethod.POST)
    public   ObjectRestResponse<ColaOrder> makeOrder(@RequestParam(required = true,value = "userId") String userId, @RequestParam(value = "price") BigDecimal price, @RequestParam(value = "code") String code, @RequestParam(value = "count") BigDecimal count, @RequestParam(value = "total") BigDecimal total, @RequestParam(value = "type") String type) ;


    @RequestMapping(value = "/balance/caculateOrder", method = RequestMethod.POST)
    public int caculateOrder(@RequestBody CaculateParams caculateParams);

    @RequestMapping(value = "/balance/matchOrder", method = RequestMethod.POST)
    public int matchOrder(@RequestBody VoCaculateParams caculateParams) ;


    @RequestMapping(value = "/balance/getOrderById", method = RequestMethod.POST)
    public ColaOrder getOrderById(@RequestParam(value = "id")String id);


    @RequestMapping(value = "/balance/getUserMoneyPassword",method = RequestMethod.GET)
    public String getUserMoneyPassword(@RequestParam(value = "id") String id);

    @RequestMapping(value = "/balance/searchOrder",method = RequestMethod.GET)
    public List<ColaOrder>searchOrder(@RequestParam(value = "userId") String userId,@RequestParam(value = "code") String code,@RequestParam(value = "state") String state,@RequestParam(value = "start") int start,@RequestParam(value = "size") int size ,@RequestParam(value = "type")String type,                @RequestParam("startTime")Long startTime,@RequestParam("endTime")Long endTime,@RequestParam("pairL")String pairL,@RequestParam("pairR")String pairR);

    @RequestMapping(value = "/balance/countSelfOrders",method = RequestMethod.GET)
    public Long countSelfOrders(@RequestParam(value = "userId") String userId,@RequestParam(value = "code") String code,@RequestParam(value = "state") String state,@RequestParam(value = "type")String type,@RequestParam("startTime")Long startTime,@RequestParam("endTime")Long endTime,@RequestParam("pairL")String pairL,@RequestParam("pairR")String pairR);

    @RequestMapping(value = "/balance/orderManagement",method = RequestMethod.GET)
    List<Map<String, Object>> orderManagement(@RequestParam("userId")String userId, @RequestParam("code")String code,
                                              @RequestParam("state")String state, @RequestParam("page")Integer page,
                                              @RequestParam("size")Integer size, @RequestParam("type")String type,
                                              @RequestParam("startTime")Long startTime, @RequestParam("endTime")Long endTime,
                                              @RequestParam("pairL")String pairL, @RequestParam("pairR")String pairR);

    @RequestMapping(value = "/balance/countOrderManagement",method = RequestMethod.GET)
    Long countOrderManagement(@RequestParam("userId")String userId, @RequestParam("code")String code,
                              @RequestParam("state")String state, @RequestParam("type")String type,
                              @RequestParam("startTime")Long startTime, @RequestParam("endTime")Long endTime,
                              @RequestParam("pairL")String pairL, @RequestParam("pairR")String pairR);

    @RequestMapping(value = "/balance/orderHistory",method = RequestMethod.GET)
    List<Map<String, Object>> orderHistory(@RequestParam("userId")String userId, @RequestParam("timestamp")Long timestamp,
                                           @RequestParam("code")String code, @RequestParam("type")String type,
                                           @RequestParam("size")Integer size,@RequestParam("isPending") Integer isPending);
}
