package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import kotlin.reflect.KClass

class PolymorphicDeserializer<T : Any>(
    val typeTable: Map<String, Map<String, KClass<out T>>>,
    val rootDelegate: (JsonParser, DeserializationContext) -> T
) : JsonDeserializer<T>() {
  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
