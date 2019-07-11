package com.bitcola.dataservice.mapper;

import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.caculate.entity.DepthLine;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户资金
 */
@Repository
public interface ColaCaculaterOrderMapper extends Mapper<ColaOrder> {

    int reduceCount(ColaOrder colaOrder);

    ColaOrder selectById(String id);

    List<ColaOrder> selectUserAndCode(@Param(value = "userid") String userId, @Param(value = "code") String code, @Param(value = "state") String state,
                                      @Param(value = "start") int start, @Param(value = "size") int size, @Param("type") String type,
                                      @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("pairL") String pairL, @Param("pairR") String pairR);

    List<DepthLine> selectBuyDepth(@Param(value = "code") String code, @Param(value = "limit") int limit, @Param(value = "precision") double precision, @Param(value = "minCountPrecision") double minCountPrecision
            , @Param(value = "time") long  time

    );

    List<DepthLine> selectSellDepth(@Param(value = "code") String code, @Param(value = "limit") int limit, @Param(value = "precision") double precision, @Param(value = "minCountPrecision") double minCountPrecision
            , @Param(value = "time") long  time
    );

    void updateEmptyOrder(@Param(value = "state") String state, @Param(value = "id") String id);

    void updateOrderState(@Param(value = "state") String state, @Param(value = "id") String id);
    void updateUnCompletedOrder(List<ColaOrder>  item);

    int updateCompletedOrder(List<String> item);

    Long countSelfOrders(@Param(value = "userid") String userId, @Param(value = "code") String code,
                         @Param(value = "state") String state, @Param("type") String type,
                         @Param("startTime") Long startTime, @Param("endTime") Long endTime,
                         @Param("pairL") String pairL, @Param("pairR") String pairR);

    List<Map<String, Object>> orderManagement(@Param("userId") String userId, @Param("code") String code, @Param("state") String state,
                                              @Param("page") Integer page, @Param("size") Integer size, @Param("type") String type,
                                              @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("pairL") String pairL,
                                              @Param("pairR") String pairR);

    Long countOrderManagement(@Param("userId") String userId, @Param("code") String code, @Param("state") String state,
                              @Param("type") String type, @Param("startTime") Long startTime, @Param("endTime") Long endTime,
                              @Param("pairL") String pairL, @Param("pairR") String pairR);

    List<Map<String, Object>> orderHistory(@Param("userId") String userId, @Param("timestamp") Long timestamp,
                                           @Param("code") String code,
                                           @Param("type") String type, @Param("size") Integer size, @Param("isPending") Integer isPending);

    List<Map<String, Object>> orderHistoryDetail(@Param("ids") List<String> ids);
}
