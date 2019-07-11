package com.bitcola.exchange.core

import com.bitcola.caculate.entity.ExchangeLog
import com.bitcola.exchange.security.common.util.Snowflake
import com.bitcola.exchange.util.BigDecimalUtils._

class ExchangeCore {


}

object ExchangeCore{
  val snowflake=new Snowflake()

  def makeLog(coinCode:String,symbol:String)(price:BigDecimal,from:BigDecimal,to:BigDecimal,fromOrderId:String,toOrderId:String,fromUserId:String,toUserId:String,fromFee:BigDecimal,toFee:BigDecimal)(types:String)={
    val id=snowflake.nextId().toString

    val exchangeLog=new ExchangeLog(id,fromUserId,toUserId,fromOrderId,toOrderId,System.currentTimeMillis(),s"${coinCode}_$symbol",types,price,from,to,fromFee,toFee,symbol,coinCode,coinCode,symbol)
    exchangeLog
  }






}
