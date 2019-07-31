package jp.justincase.jackson.kotlin.numeric

import java.math.BigDecimal

interface Numeric<T : Any> : NumericEncoder<T>, NumericDecoder<T>

interface NumericEncoder<in T : Any> {
  val T.decimal: BigDecimal
}

interface NumericDecoder<out T : Any> {
  fun fromDecimal(value: BigDecimal): T

  fun fromNull(): T? = null
}
