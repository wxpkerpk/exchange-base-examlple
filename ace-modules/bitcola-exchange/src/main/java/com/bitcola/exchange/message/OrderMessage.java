package com.bitcola.exchange.message;


import com.bitcola.exchange.constant.OrderDirection;
import com.bitcola.exchange.constant.OrderStatus;
import com.bitcola.exchange.constant.OrderType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Table(name = "ag_admin_v1.cola_exchange_order")
public class OrderMessage implements Serializable {
	@Id
	public String id;
	@Column(name = "user_id")
	public String userId;
	public long timestamp;
	/**
	 * 交易对 (EOS_USDT)
	 */
	public String pair;
	/**
	 * 买还是卖
	 * @see OrderDirection
	 */
	public String direction;
	/**
	 * 挂单价
	 */
	public BigDecimal price;
	/**
	 * 挂单数量
	 */
	public BigDecimal number;
	/**
	 * 剩余未撮合数量
	 */
	public BigDecimal remain;
	/**
	 * 订单状态
	 * @see OrderStatus
	 */
	public String status;
	/**
	 * 订单类型
	 * @see OrderType
	 */
	public String type;

	/**
	 * 成交均价
	 */
	@Column(name = "average_price")
	public BigDecimal averagePrice;
	/**
	 * 费率
	 */
	@Column(name = "fee_rate")
	public BigDecimal feeRate;

	@Override
	public String toString() {
		return String.format("%s: %s orderId=%s", this.getClass().getSimpleName(), this.direction,
				 this.id);
	}

	public String coinCode(){
		return this.pair.split("_")[0];
	}
	public String symbol(){
		return this.pair.split("_")[1];
	}

	@Override
	public boolean equals(Object object){
		return ((OrderMessage)object).getId().equals(this.getId());
	}

}
