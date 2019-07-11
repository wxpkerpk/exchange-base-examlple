package com.bitcola.exchange.core

import akka.actor.Actor
import com.bitcola.caculate.entity.ColaOrder

class ScalaTest {

}



object ScalaTest extends App {

  val treeMap=collection.mutable.TreeMap[String,ColaOrder]()





}
import akka.actor.{ActorSystem, Props, Actor}
import akka.routing.RoundRobinPool

/**
  * Created with IntelliJ IDEA.
  * User: criss
  * Date: 13-2-21
  * Time: 上午1:37
  * To change this template use File | Settings | File Templates.
  */
object Pi extends App {
  //Actor传递消息特质，所有的消息都实现这个特质
  sealed trait PieMessage
  //工作线程的运行指令，start:分段开始的值，nrOfElements:本段的数目
  case class Work(start: Int, nrOfElements: Int) extends PieMessage
  //工作线程结果返回指令
  case class Result(value:Double) extends PieMessage
  //主线程运行指令
  case object Calculate extends PieMessage

  caculate(10000,10,10000)

  class Worker extends Actor {

    def caculate(start:Int,nf:Int) = {
      var acc:Double = 0.0
      //计算start到start+nf的数值，详见上面公式
      for (i <- start until start+nf)
      {
        acc +=  4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
      }

      acc
    }

    def receive = {
      //处理运行指令
      case Work(start, nfOfElement) =>
        //返回给主线程结果
        sender !  Result(caculate(start,nfOfElement))
    }
  }
  //nrOfMesseage 工作线程数
  //nfWorks工作线程调度池的大小

  //nrOfElements每个工作线程计算的数值大小
  class Master(nrOfMesseage:Int,nfWorks:Int,nrOfElements:Int) extends Actor
  {
    //最后的结果
    var pi:Double = _
    //计数，计算已经运行完多少个Worker
    var nrOfResults:Int = 0
    //你可以理解成一个线程池，用于调度Worker
    val workerRouter = context.actorOf(
      Props[Worker].withRouter(RoundRobinPool(nfWorks)),name="workers"
    )
    //存储开始时间
    var start:Long  = _
    def receive = {
      case Calculate =>
        start = System.currentTimeMillis
        for (i <- 0 until nrOfMesseage)
          workerRouter ! Work(i*nrOfElements,nrOfElements)
      case Result(value) =>
        //当有一个结果返回时，合并结果
        pi += value
        //计算当前返 回的结果总数
        nrOfResults += 1
        //当全部结果返回时，打印结果退出
        if(nrOfResults == nrOfMesseage)
        {
          println(pi)
          println("耗时:"+(System.currentTimeMillis()-start))
        }

    }
  }

  def caculate(nrOfMesseage:Int,nfWorks:Int,nrOfElements:Int)
  {
    //初始化ActorSystem
    val system = ActorSystem("PiSystem")
    //初始化主线程
    val master = system.actorOf(Props(new Master(nrOfMesseage
      ,nfWorks,nrOfElements)),name="master")
    //发送计算指令
    master ! Calculate
  }



}