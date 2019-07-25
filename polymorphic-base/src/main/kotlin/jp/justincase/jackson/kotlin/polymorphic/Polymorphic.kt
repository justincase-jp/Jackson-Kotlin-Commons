package jp.justincase.jackson.kotlin.polymorphic

import kotlin.reflect.KClass

interface Polymorphic {
  val typeKey
    get() = "type"

  val KClass<out Any>.toTypeName: String
    get() = simpleName ?: throw IllegalArgumentException(toString())
}
