package jp.justincase.jackson.kotlin.textual.codec

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import jp.justincase.jackson.kotlin.textual.TextualSerializer
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance

private
class Serializer<T : Any>(
    private val delegate: (T) -> String,
    private val fallback: (T, JsonGenerator, SerializerProvider) -> Unit
) : JsonSerializer<T>() {
  override
  fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider) =
      // We should reject unrelated types,
      // but by now we will assume that only related type is used in the type parameter
      // util we found a better way to confirm sub-typing
      try {
        gen.writeString(delegate(value))
      } catch (e: Exception) {
        when (e) {
          is ClassCastException, is NullPointerException ->
            fallback(value, gen, serializers)
          else ->
            throw e
        }
      }
}


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
          .mapNotNull { it.companionObjectInstance as? TextualSerializer<Nothing> }
          .map { textual ->
            // Catch for ClassCastException and NullPointerException by now
            @Suppress("UNCHECKED_CAST")
            val t = textual as TextualSerializer<T>

            Serializer({ t.run { it.text } }, serializer::serialize)
          }
          .firstOrNull()
          .let {
            // Aid type inference
            val result: JsonSerializer<in T> = it ?: serializer
            result
          }
}
