package com.bitcola.exchange.mapper;

import com.bitcola.exchange.message.OrderMessage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface OrderMapper extends Mapper<OrderMessage> {

    List<OrderMessage> selectUnSuccessOrder(@Param("pair") String pair,@Param("size") int size,@Param("page") int page);

    int insertOrder(OrderMessage order);

    List<OrderMessage> selectOrders(List<String> ids);

    void batchUpdate(List<OrderMessage> orderList);

    List<OrderMessage> selectOrderByPair(@Param("pair")String pair, @Param("userId")String userId);
}
