package jp.justincase.jackson.kotlin.numerical.codec

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import jp.justincase.jackson.kotlin.internal.allNonInterfaceSuperclassesAndInterfaces
import jp.justincase.jackson.kotlin.internal.effectiveCompanion
import jp.justincase.jackson.kotlin.internal.primitive.codec.PrimitiveSerializer
import jp.justincase.jackson.kotlin.numerical.NumericalEncoder

internal
object NumericalSerializerModifier : BeanSerializerModifier() {
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
      (sequenceOf(beanDesc.beanClass.kotlin) + beanDesc.beanClass.kotlin.allNonInterfaceSuperclassesAndInterfaces)
          .mapNotNull { it.effectiveCompanion as? NumericalEncoder<Nothing> }
          .map { textual ->
            // Catch for ClassCastException and NullPointerException by now
            @Suppress("UNCHECKED_CAST")
            val t = textual as NumericalEncoder<T>

            PrimitiveSerializer({ t.run { it.decimal } }, serializer::serialize, JsonGenerator::writeNumber)
          }
          .firstOrNull()
          .let {
            // Aid type inference
            val result: JsonSerializer<in T> = it ?: serializer
            result
          }
}
