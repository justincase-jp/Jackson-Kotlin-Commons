package jp.justincase.jackson.kotlin.textual.codec

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.google.common.reflect.TypeToken
import jp.justincase.jackson.kotlin.internal.reportInputMismatch
import jp.justincase.jackson.kotlin.internal.toTypeToken
import jp.justincase.jackson.kotlin.textual.TextualDeserializer
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.safeCast

private
class Deserializer<T : Any>(
    private val delegate: (String) -> T,
    private val nullDelegate: () -> T?
) : JsonDeserializer<T>() {
  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
      p.text.let {
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

private
class SuperDeserializer<T : Any>(
    private val subtype: KClass<T>,
    private val delegate: (String) -> Any,
    private val nullDelegate: () -> Any?,
    private val fallback: (JsonParser, DeserializationContext) -> T,
    private val nullFallback: (DeserializationContext) -> T?
) : JsonDeserializer<T>() {
  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
      subtype.safeCast(p.text.let {
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
      (sequenceOf(beanDesc.beanClass.kotlin) + beanDesc.beanClass.kotlin.allSuperclasses.asSequence())
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

                Deserializer(
                    it::fromText,
                    it::fromNull
                )
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
                    it::fromNull,
                    deserializer::deserialize,
                    deserializer::getNullValue
                )
              }
            }
          }
          .firstOrNull()
          .let { it ?: deserializer }
}
