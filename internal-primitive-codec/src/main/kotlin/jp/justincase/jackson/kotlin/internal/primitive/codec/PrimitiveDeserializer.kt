package jp.justincase.jackson.kotlin.internal.primitive.codec

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import jp.justincase.jackson.kotlin.internal.reportInputMismatch
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

class PrimitiveDeserializer<T : Any, R : Any>(
    private val delegate: (R) -> T,
    private val nullDelegate: () -> T?,
    private val reader: (JsonParser) -> R
) : JsonDeserializer<T>() {
  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
      reader(p).let {
        try {
          delegate(it)
        } catch (e: IllegalArgumentException) {
          throw reportInputMismatch(e, ctxt, "$e")
        }
      }

  override
  fun getNullValue(ctxt: DeserializationContext): T? =
      try {
        nullDelegate()
      } catch (e: IllegalArgumentException) {
        throw reportInputMismatch(e, ctxt, "$e")
      }
}

class PrimitiveSuperDeserializer<T : Any, R : Any>(
    private val subtype: KClass<T>,
    private val delegate: (R) -> Any,
    private val nullDelegate: () -> Any?,
    private val reader: (JsonParser) -> R,
    private val fallback: (JsonParser, DeserializationContext) -> T,
    private val nullFallback: (DeserializationContext) -> T?
) : JsonDeserializer<T>() {
  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
      subtype.safeCast(reader(p).let {
        try {
          delegate(it)
        } catch (e: IllegalArgumentException) {
          throw reportInputMismatch(e, ctxt, "$e")
        }
      }) ?: fallback(p, ctxt)

  override
  fun getNullValue(ctxt: DeserializationContext): T? =
      subtype.safeCast(try {
        nullDelegate()
      } catch (e: IllegalArgumentException) {
        throw reportInputMismatch(e, ctxt, "$e")
      }) ?: nullFallback(ctxt)
}
