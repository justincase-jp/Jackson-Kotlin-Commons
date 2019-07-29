package jp.justincase.jackson.kotlin.textual

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.google.common.reflect.TypeToken
import jp.justincase.jackson.kotlin.core.allNonInterfaceSuperclasses
import jp.justincase.jackson.kotlin.core.toTypeToken
import kotlin.reflect.full.companionObjectInstance

private
class Deserializer<T : Any>(private val delegate: (String) -> T) : JsonDeserializer<T>() {
  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
      delegate(p.text)
}

internal
object TextualDeserializerModifier : BeanDeserializerModifier() {
  override
  fun modifyDeserializer(
      config: DeserializationConfig,
      beanDesc: BeanDescription,
      deserializer: JsonDeserializer<*>
  ): JsonDeserializer<*> =
      modifyCovariantDeserializer(beanDesc, deserializer)

  private
  fun <T : Any> modifyCovariantDeserializer(
      beanDesc: BeanDescription,
      deserializer: JsonDeserializer<out T>
  ): JsonDeserializer<out T> =
      beanDesc
          .beanClass
          .kotlin
          .allNonInterfaceSuperclasses
          .mapNotNull { t ->
            (t.companionObjectInstance as? TextualDeserializer<Any>)?.takeIf {
              @Suppress("UnstableApiUsage")
              TypeToken
                  .of(it.javaClass)
                  .getSupertype(TextualDeserializer::class.java)
                  .resolveType(TextualDeserializer::class.java.typeParameters.first())
                  .isSubtypeOf(beanDesc.type.toTypeToken())
            }
          }
          .firstOrNull()
          ?.let { textual ->
            // The first parameter is a subtype of `T`
            @Suppress("UNCHECKED_CAST")
            textual as TextualDeserializer<T>

            Deserializer(textual::fromText)
          }
          .let { it ?: deserializer }
}
