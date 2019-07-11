package com.bitcola.exchange.util

class BigDecimalUtils {

}
object BigDecimalUtils{

  implicit def javaBigdicimal2scalaBigdicimal(d: java.math.BigDecimal) = scala.math.BigDecimal(d)
  implicit def scalaBigdicimal2javaBigdicimal(d:scala.math.BigDecimal) = d.bigDecimal

}
