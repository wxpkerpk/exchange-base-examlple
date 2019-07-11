package com.bitcola.exchange.core


import com.bitcola.caculate.entity.{ColaOrder, DepthData}
import com.bitcola.exchange.util.BigDecimalUtils._

import scala.collection.JavaConverters._

class OrderQueue {
  implicit object obj1 extends Ordering[ColaOrder]{
    override def compare(x: ColaOrder, y: ColaOrder): Int = {
      if(x.getPrice>y.getPrice) 1
      else if(x.getPrice==y.getPrice){
        x.getId.compareTo(y.getId)
      }
      else -1
    }
  }
  implicit object obj2 extends Ordering[ColaOrder]{
    override def compare(x: ColaOrder, y: ColaOrder): Int = {
      if(x.getPrice<y.getPrice) 1
      else if(x.getPrice==y.getPrice){
        x.getId.compareTo(y.getId)
      }
      else -1
    }
  }
  val orderMap = collection.mutable.HashMap[String, ColaOrder]()

  val sellOrders = collection.mutable.TreeMap[ColaOrder, ColaOrder]()(obj1)
  //卖出订单的优先列队
  val buyOrders = collection.mutable.TreeMap[ColaOrder, ColaOrder]()(obj2) //买入订单的优先列队

  def getOrderKey(order: ColaOrder) = {
    order

  }

  def getDepth(scale: Int, len: Int) = {

    val asks = sellOrders.toArray.map(x => (x._2.getPrice.setScale(scale), x._2.getCount)).groupBy(_._1).mapValues(y => {
      val re= y.reduce((a1, a2) => {
        (a1._1, a1._2 + a2._2)

      })
      Array(re._1.doubleValue(),re._2.doubleValue())

    }).values.take(len).toArray.reverse
    val bids = buyOrders.toArray.map(x => (x._2.getPrice.setScale(scale), x._2.getCount)).groupBy(_._1).mapValues(y => {
      val re= y.reduce((a1, a2) => {
        (a1._1, a1._2 + a2._2)

      })
      Array(re._1.doubleValue(),re._2.doubleValue())
    }).values.take(len).toArray.reverse

    Map("ask" -> asks, "bids" -> bids).asJava
  }


  def putOrder(order: ColaOrder) = {
    val key = getOrderKey(order)

    order.getType match {
      case "buy" => {
        buyOrders.synchronized {

          buyOrders += (key -> order)
          orderMap += (order.getId -> order)
        }
      }
      case "sell" => {
        sellOrders.synchronized {

          sellOrders += (key -> order)
          orderMap += (order.getId -> order)
        }

      }
    }
  }

  def recoverOrder(order:ColaOrder)={
    if(orderMap.contains(order.getId)){
      val queueOrder=orderMap.get(order.getId).get
      queueOrder.setCount(order.getCount)
      queueOrder.setTotal(order.getTotal)
    }else{
      putOrder(order)
    }



  }

  def getCurrentBuyOrder() = {
    if (buyOrders.nonEmpty) {
      buyOrders.head._2
    } else null
  }

  def getCurrentSellOrder() = {
    if (sellOrders.nonEmpty) {
      sellOrders.head._2
    } else null
  }

  def removeCurrentBuyOrder() = {
    buyOrders.synchronized {
      if (buyOrders.nonEmpty) {
        val head = buyOrders.head
        orderMap -= head._2.getId
        buyOrders -= head._1
      }
    }

  }

  def removeCurrentSellOrder() = {
    sellOrders.synchronized {
      if (sellOrders.nonEmpty) {

        val head = sellOrders.head
        orderMap -= head._2.getId
        sellOrders -= head._1
      }
    }
  }

  def removeOrder(id: String) = {
    val order = orderMap(id)
    if (order != null) {
      if (orderMap.contains(id)) {
        orderMap -= id
        order.getType match {
          case "buy" => {
            buyOrders.synchronized {
              buyOrders -= getOrderKey(order)
            }
          }
          case "sell" => {
            sellOrders.synchronized {
              sellOrders -= getOrderKey(order)
            }
          }
        }
      }
      null
    }


  }


}
