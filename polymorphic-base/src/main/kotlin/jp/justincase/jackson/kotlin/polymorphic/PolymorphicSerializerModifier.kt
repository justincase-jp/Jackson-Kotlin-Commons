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
      modifyContravariantSerializer(config, beanDesc, serializer)

  private
  fun <T : Any> modifyContravariantSerializer(
      config: SerializationConfig,
      beanDesc: BeanDescription,
      serializer: JsonSerializer<in T>
  ): JsonSerializer<in T> {
    val beanClass = beanDesc.beanClass.kotlin

    // Assume the caller is correct
    @Suppress("UNCHECKED_CAST")
    beanClass as KClass<T>

    val modified = beanClass
        .allNonInterfaceSuperclasses
        .mapNotNull { it.companionObjectInstance as? Polymorphic }
        .firstOrNull()
        ?.let {
          PolymorphicSerializer(it, serializer.unwrappingSerializer(null)::serialize)
        }

    return modified ?: serializer
  }
}
