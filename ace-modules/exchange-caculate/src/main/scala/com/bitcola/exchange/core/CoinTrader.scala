package com.bitcola.exchange.core

import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

import scala.collection.JavaConverters._
import com.bitcola.caculate.entity.{ColaOrder, ExchangeLog}
import com.bitcola.exchange.security.common.util.Snowflake
import com.bitcola.exchange.util.BigDecimalUtils._
import com.bitcola.caculate.entity.Vo._
import com.bitcola.exchange.caculate.dataservice.ColaBalanceService
import org.springframework.stereotype.Service

import scala.collection.mutable.ListBuffer

object CoinTrader extends App {
  val snowflake=new Snowflake()


  val coinTrader = new CoinTrader("BTC_USDT")
  val order1 = new ColaOrder()
  val pair = "BTC_USDT"
  coinTrader.makeOrder("2", "sell", 2, 8, pair)

  coinTrader.makeOrder("2", "sell", 1, 8, pair)
  coinTrader.makeOrder("2", "sell", 1, 8, pair)
  coinTrader.makeOrder("2", "sell", 1, 8, pair)

  coinTrader.makeOrder("1", "buy", 2, 1, pair)
  coinTrader.makeOrder("1", "buy", 3, 1, pair)
  coinTrader.makeOrder("1", "buy", 10, 10, pair)
  val re= coinTrader.getDepth(5,1)
  coinTrader.matchService()



}


case class MoneyFeedBack(userId: String, coinCode: String, count: BigDecimal)

case class OrderState(completed: collection.mutable.ListBuffer[String], transforms: collection.mutable.ListBuffer[Transforms], moneyCallBack: collection.mutable.ListBuffer[MoneyFeedBack], var unCompletedOrder: ColaOrder)

case class Transforms(userId: String, from: BigDecimal, to: BigDecimal, fromCode: String, toCode: String)

case class OrderChange(id: String, count: BigDecimal, total: BigDecimal)


class CoinTrader(val pair: String) {
  var balanceService: ColaBalanceService = _

  var feesFactor: Double = _

  val coinCode = pair.split("_")(0)
  val symbol = pair.split("_")(1)
  val makeLog = ExchangeCore.makeLog(coinCode, symbol) _


  val orderCache=collection.mutable.ListBuffer[ColaOrder]()

  var lastSum:Double= _

  var lastPrice:Double=_
  val orderQueue = new OrderQueue
  val cancelQueue= new  ConcurrentLinkedQueue[ColaOrder]()


  def makeOrder(userId: String, types: String, price: BigDecimal, count: BigDecimal, pair: String): Unit = {
    val order = new ColaOrder()
    order.setCoinCode(pair)
    order.setCount(count)
    order.setPrice(price)
    order.setId(snowflake.nextIdStr())
    order.setUserId(userId)
    order.setTime(System.currentTimeMillis())
    order.setType(types)
    if (types.equals("buy")) {
      val total = price * count
      order.setOriginTotal(total)
      order.setTotal(total)
    } else {
      order.setOriginTotal(count)
    }
    putOrder(order)
  }

  def putOrder(colaOrder: ColaOrder): Unit = {
    orderQueue.putOrder(colaOrder)
  }


  def getDepth(len:Int,scale:Int)=
  {

    val depth= orderQueue.getDepth(scale,len)
    depth

  }

  case class SystemIn(coinCodeIn: BigDecimal, symbolIn: BigDecimal)

  def processMatch(focusOrder: ColaOrder, otherOrder: ColaOrder) = {
    val (buyOrder, sellOrder) = focusOrder.getType match {
      case "buy" => (focusOrder, otherOrder)
      case "sell" => (otherOrder, focusOrder)
    }
    var payback: MoneyFeedBack = null
    val transformsList = collection.mutable.ListBuffer[Transforms]()
    val logs = collection.mutable.ListBuffer[ExchangeLog]()

    val (buyPrice, sellPrice) = (buyOrder.getPrice, sellOrder.getPrice)
    if (buyPrice >= sellPrice) {
      val (buyCount, sellCount) = (buyOrder.getCount, sellOrder.getCount)
      val price = otherOrder.getPrice
      lastPrice=price.doubleValue()
      if (buyCount > sellCount) {
        buyOrder.setCount(buyCount - sellCount)
        buyOrder.setTotal(buyOrder.getTotal - sellCount * price)
        sellOrder.setCount(BigDecimal.valueOf(0))
        val transforms = Transforms(sellOrder.getUserId, sellCount, sellCount * price, coinCode, symbol)
        transformsList += transforms
        transformsList += Transforms(buyOrder.getUserId, sellCount * price, sellCount , symbol, coinCode)
        val log = makeLog(price, sellCount, sellCount * price, sellOrder.getId, buyOrder.getId, sellOrder.getUserId, buyOrder.getUserId, 0, 0)(focusOrder.getType)
        logs += log
      } else {
        val remind = sellCount - buyCount
        buyOrder.setCount(BigDecimal.valueOf(0))
        buyOrder.setTotal(buyOrder.getTotal - buyCount * price)
        if (buyOrder.getTotal > 0) payback = MoneyFeedBack(buyOrder.getUserId, symbol, buyOrder.getTotal)
        val transforms = Transforms(buyOrder.getUserId, buyCount * price, buyCount, symbol, coinCode)
        transformsList += Transforms(sellOrder.getUserId, buyCount, buyCount * price, coinCode, symbol)
        transformsList += transforms
        val log = makeLog(price, buyCount, buyCount * price, sellOrder.getId, buyOrder.getId, sellOrder.getUserId, buyOrder.getUserId, 0, 0)(focusOrder.getType)
        sellOrder.setCount(remind)
        logs += log
      }
    }
    (transformsList, payback, logs)
  }

  implicit class ExchangeColaOrder(order: ColaOrder) {
    def isCompeleted() = {
      order.getCount <= 0
    }

     def cloneOrder(): AnyRef ={

       val newOrder=new ColaOrder();
       newOrder.setId(order.getId)
       newOrder.setPrice(order.getPrice)
       newOrder.setTime(order.getTime)
       newOrder.setCoinCode(order.getCoinCode)
       newOrder.setCount(BigDecimal.apply(order.getCount))
       newOrder.setOriginTotal(BigDecimal.apply(order.getOriginTotal))
       newOrder.setTotal(BigDecimal.apply(order.getTotal))
       newOrder.setType(order.getType)
       newOrder.setUserId(order.getUserId)
       newOrder

     }



    def isUnCompeleted() = {
      order.getType match {
        case "buy" => order.getOriginTotal >= order.getTotal && order.getCount > 0
        case "sell" => order.getOriginTotal >= order.getCount && order.getCount > 0
      }
    }

  }




  val snowflake = new Snowflake

  def matchService() = {
    lastSum=0
    var continue = true
    val completedOrders = collection.mutable.ListBuffer[String]() //完成订单的id
    var unCompletedOrder= collection.mutable.ListBuffer[ColaOrder]()// 未完成订单的资金改变
    var transformsResult = collection.mutable.ListBuffer[Transforms]()
    // 用户的资金转移
    var payBacks = collection.mutable.ListBuffer[MoneyFeedBack]() //买单完成的用户余额返还
    var visited = collection.mutable.ListBuffer[ColaOrder]()
    val exchangeLogs = collection.mutable.ListBuffer[ExchangeLog]()

    def cleanState(currentBuyOrder: ColaOrder, currentSellOrder: ColaOrder, state: (ListBuffer[Transforms], MoneyFeedBack, ListBuffer[ExchangeLog])) = {
      if (currentBuyOrder.isCompeleted()) {
        completedOrders += currentBuyOrder.getId
        orderQueue.removeCurrentBuyOrder()
      }
      if (currentSellOrder.isCompeleted()) {
        completedOrders += currentSellOrder.getId
        orderQueue.removeCurrentSellOrder()
      }
      exchangeLogs++=state._3
      transformsResult ++= state._1
      payBacks += state._2
      if (state._1.nonEmpty) {
        visited += currentBuyOrder
        visited += currentSellOrder
      } else {
        continue = false
      }
    }

    while (continue) {

      val currentBuyOrder = orderQueue.getCurrentBuyOrder()
      val currentSellOrder = orderQueue.getCurrentSellOrder()
      if (currentBuyOrder != null && currentSellOrder != null) {

        orderCache += currentBuyOrder
        orderCache +=currentSellOrder
        if (currentSellOrder.getTime >= currentBuyOrder.getTime) {
          val state = processMatch(currentBuyOrder, currentSellOrder)
          cleanState(currentBuyOrder, currentSellOrder, state)
          if(state._1.isEmpty) continue=false

        } else {
          val state = processMatch(currentSellOrder, currentBuyOrder)
          cleanState(currentBuyOrder, currentSellOrder, state)
          if(state._1.isEmpty) continue=false

        }
      } else {
        continue = false
      }
    }
    visited=visited.filter(_!=null).distinct
    unCompletedOrder = visited.filter(x => x.isUnCompeleted())
    val transformsResultArray = transformsResult.groupBy(x => (x.userId, x.fromCode, x.toCode)).mapValues(y => {
      y.reduce((a1, a2) => Transforms(a1.userId, a1.from + a2.from, a1.to + a2.to, a1.fromCode, a1.toCode))
    }).values.map(t => {
      new TransForms(t.userId, t.from, t.to, t.fromCode, t.toCode)
    }).toList
    val payBacksArray = payBacks.filter(_!=null).groupBy(_.userId).mapValues(x => {
      x.reduce((a1, a2) => {
        MoneyFeedBack(a1.userId, a1.coinCode, a1.count + a2.count)
      })
    }).filter(_!=null).map(v => {
      new Payback(v._2.userId, v._2.count, v._2.coinCode)
    }).toList
    if(transformsResultArray.nonEmpty) {
      lastSum = transformsResultArray.filter(_.getFromCode.equals(symbol)).map(_.getFrom).reduce((a1, a2) => a1 + a2).doubleValue()
    }


    (new VoCaculateParams(completedOrders.distinct.asJava, unCompletedOrder.asJava, transformsResultArray.asJava, payBacksArray.asJava, exchangeLogs.asJava,visited.asJava),orderCache.distinct.asJava)

  }
  def setSuccess()=
  {
    orderCache.clear()
    lastSum=0

  }


}



