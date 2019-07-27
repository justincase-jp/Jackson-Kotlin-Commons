package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

internal
class PolymorphicWrappedSerializer<T : Any>(
    private val typeKey: String,
    private val typeName: String,
    private val valueKey: String,
    private val wrappedDelegate: (T, JsonGenerator, SerializerProvider) -> Unit
) : JsonSerializer<T>() {
  override
  fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeStartObject()
    gen.writeStringField(typeKey, typeName)
    gen.writeFieldName(valueKey)
    wrappedDelegate(value, gen, serializers)
    gen.writeEndObject()
  }
}
