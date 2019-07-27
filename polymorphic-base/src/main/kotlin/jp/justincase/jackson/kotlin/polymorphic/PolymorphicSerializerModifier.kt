package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

internal
object PolymorphicSerializerModifier : BeanSerializerModifier() {
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
  ): JsonSerializer<in T> {
    val beanClass = beanDesc.beanClass.kotlin

    // Assume the caller is correct
    @Suppress("UNCHECKED_CAST")
    beanClass as KClass<T>

    val modified = beanClass
        .allNonInterfaceSuperclasses
        .zipWithNext()
        .firstOrNull { (_, t) -> t.isSealed }
        ?.let { (type, _) ->
          type
              .allNonInterfaceSuperclasses
              .mapNotNull { it.companionObjectInstance as? Polymorphic }
              .firstOrNull()
              ?.let { type to it }
        }
        ?.let { (type, p) ->
          p.run {
            when (val valueKey = valueKey) {
              null -> PolymorphicSerializer(
                  typeKey,
                  type.toTypeName,
                  serializer.unwrappingSerializer(null)::serialize
              )
              else -> PolymorphicWrappedSerializer(
                  typeKey,
                  type.toTypeName,
                  valueKey,
                  serializer::serialize
              )
            }
          }
        }

    return modified ?: serializer
  }
}
