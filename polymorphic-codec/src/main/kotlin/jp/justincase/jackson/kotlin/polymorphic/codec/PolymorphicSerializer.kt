package jp.justincase.jackson.kotlin.polymorphic.codec

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

internal
class PolymorphicSerializer<T : Any>(
    private val typeKey: String,
    private val typeName: String,
    private val unwrappedDelegate: (T, JsonGenerator, SerializerProvider) -> Unit
) : JsonSerializer<T>() {
  override
  fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeStartObject()
    gen.writeStringField(typeKey, typeName)
    unwrappedDelegate(value, gen, serializers)
    gen.writeEndObject()
  }
}
