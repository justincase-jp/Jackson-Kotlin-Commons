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
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.safeCast

private
class Deserializer<T : Any>(
    private val delegate: (String) -> T
) : JsonDeserializer<T>() {
  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
      delegate(p.text)
}

private
class SuperDeserializer<T : Any>(
    private val subtype: KClass<T>,
    private val delegate: (String) -> Any,
    private val fallback: (JsonParser, DeserializationContext) -> T
) : JsonDeserializer<T>() {
  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
      subtype.safeCast(delegate(p.text)) ?: fallback(p, ctxt)
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
          .mapNotNull { it.companionObjectInstance as? TextualDeserializer<Any> }
          .map {
            @Suppress("UnstableApiUsage")
            val typeToken = TypeToken
                .of(it.javaClass)
                .getSupertype(TextualDeserializer::class.java)
                .resolveType(TextualDeserializer::class.java.typeParameters.first())
            val baseTypeToken = beanDesc.type.toTypeToken()

            when {
              typeToken.isSubtypeOf(baseTypeToken) -> {
                // The first parameter is a subtype of `T`
                @Suppress("UNCHECKED_CAST")
                it as TextualDeserializer<T>

                Deserializer(it::fromText)
              }
              // We should reject unrelated types,
              // but by now we will assume that only related type is used in the type parameter
              // util we found a better way to confirm sub-typing
              else -> {
                // The requested raw type
                @Suppress("UNCHECKED_CAST")
                val requestedRawType = (baseTypeToken.rawType as Class<*>).kotlin as KClass<T>

                SuperDeserializer(
                    requestedRawType,
                    it::fromText,
                    deserializer::deserialize
                )
              }
            }
          }
          .firstOrNull()
          .let { it ?: deserializer }
}
