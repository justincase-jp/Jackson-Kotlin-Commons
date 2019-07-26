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

    val modified = (sequenceOf(beanClass) + beanClass.allNonInterfaceSuperclasses.takeWhile(KClass<in T>::isSealed))
        .mapNotNull { it.companionObjectInstance as? Polymorphic }
        .firstOrNull()
        ?.let {
          if (it.valueKey == null) {
            PolymorphicSerializer(it, serializer.unwrappingSerializer(null)::serialize)
          } else {
            PolymorphicWrappedSerializer(it, serializer::serialize)
          }
        }

    return modified ?: serializer
  }
}
