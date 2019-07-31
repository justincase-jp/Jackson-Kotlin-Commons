package jp.justincase.jackson.kotlin.numerical

import java.math.BigDecimal

interface Numerical<T : Any> : NumericalEncoder<T>, NumericalDecoder<T>

interface NumericalEncoder<in T : Any> {
  val T.decimal: BigDecimal
}

interface NumericalDecoder<out T : Any> {
  fun fromDecimal(value: BigDecimal): T

  fun fromNull(): T? = null
}
