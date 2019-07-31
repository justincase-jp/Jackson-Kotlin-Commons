package jp.justincase.jackson.kotlin.internal.primitive.codec

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class PrimitiveSerializer<T : Any, R : Any>(
    private val delegate: (T) -> R,
    private val fallback: (T, JsonGenerator, SerializerProvider) -> Unit,
    private val writer: (JsonGenerator, R) -> Unit
) : JsonSerializer<T>() {
  override
  fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider) =
      // We should reject unrelated types,
      // but by now we will assume that only related type is used in the type parameter
      // util we found a better way to confirm sub-typing
      try {
        writer(gen, delegate(value))
      } catch (e: Exception) {
        when (e) {
          is ClassCastException, is NullPointerException ->
            fallback(value, gen, serializers)
          else ->
            throw e
        }
      }
}
