package jp.justincase.jackson.kotlin.textual.codec

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.google.common.reflect.TypeToken
import jp.justincase.jackson.kotlin.internal.effectiveCompanion
import jp.justincase.jackson.kotlin.internal.primitive.codec.PrimitiveDeserializer
import jp.justincase.jackson.kotlin.internal.primitive.codec.PrimitiveSuperDeserializer
import jp.justincase.jackson.kotlin.internal.toTypeToken
import jp.justincase.jackson.kotlin.textual.TextualDecoder
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

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
          .mapNotNull {
            it.effectiveCompanion?.let { c ->
              c as? TextualDecoder<Any> ?: c.enumeratedAsTextualDecoder
            }
          }
          .map {
            @Suppress("UnstableApiUsage")
            val typeToken = TypeToken
                .of(it.javaClass)
                .getSupertype(TextualDecoder::class.java)
                .resolveType(TextualDecoder::class.java.typeParameters.first())
            val baseTypeToken = beanDesc.type.toTypeToken()

            when {
              typeToken.isSubtypeOf(baseTypeToken) -> {
                // The first parameter is a subtype of `T`
                @Suppress("UNCHECKED_CAST")
                it as TextualDecoder<T>

                PrimitiveDeserializer(
                    it::fromText,
                    it::fromNull,
                    JsonParser::getText
                )
              }
              // We should reject unrelated types,
              // but by now we will assume that only related type is used in the type parameter
              // util we found a better way to confirm sub-typing
              else -> {
                // The requested raw type
                @Suppress("UNCHECKED_CAST")
                val requestedRawType = (baseTypeToken.rawType as Class<*>).kotlin as KClass<T>

                PrimitiveSuperDeserializer(
                    requestedRawType,
                    it::fromText,
                    it::fromNull,
                    JsonParser::getText,
                    deserializer::deserialize,
                    deserializer::getNullValue
                )
              }
            }
          }
          .firstOrNull()
          .let { it ?: deserializer }
}
