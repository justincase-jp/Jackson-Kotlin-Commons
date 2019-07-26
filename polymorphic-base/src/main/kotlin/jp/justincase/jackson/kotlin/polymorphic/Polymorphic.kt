package jp.justincase.jackson.kotlin.polymorphic

import kotlin.reflect.KClass

interface Polymorphic {
  val typeKey: String
    get() = "type"

  val valueKey: String?
    get() = null

  val KClass<out Any>.toTypeName: String
    get() = simpleName ?: throw IllegalArgumentException(toString())
}
