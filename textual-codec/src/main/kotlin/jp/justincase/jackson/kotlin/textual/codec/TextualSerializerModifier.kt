package jp.justincase.jackson.kotlin.textual.codec

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import jp.justincase.jackson.kotlin.internal.effectiveCompanion
import jp.justincase.jackson.kotlin.internal.primitive.codec.PrimitiveSerializer
import jp.justincase.jackson.kotlin.textual.TextualEncoder
import kotlin.reflect.full.allSuperclasses

internal
object TextualSerializerModifier : BeanSerializerModifier() {
  override
  fun modifySerializer(
      config: SerializationConfig,
      beanDesc: BeanDescription,
      serializer: JsonSerializer<*>
  ): JsonSerializer<*> =
      modifyContravariantSerializer(beanDesc, serializer)

  private
  fun <T : Any> modifyContravariantSerializer(
      beanDesc: BeanDescription,
      serializer: JsonSerializer<in T>
  ): JsonSerializer<in T> =
      (sequenceOf(beanDesc.beanClass.kotlin) + beanDesc.beanClass.kotlin.allSuperclasses.asSequence())
          .mapNotNull {
            it.effectiveCompanion?.let { c ->
              c as? TextualEncoder<Nothing> ?: c.enumeratedAsTextualEncoder
            }
          }
          .map { textual ->
            // Catch for ClassCastException and NullPointerException by now
            @Suppress("UNCHECKED_CAST")
            val t = textual as TextualEncoder<T>

            PrimitiveSerializer({ t.run { it.text } }, serializer::serialize, JsonGenerator::writeString)
          }
          .firstOrNull()
          .let {
            // Aid type inference
            val result: JsonSerializer<in T> = it ?: serializer
            result
          }
}
