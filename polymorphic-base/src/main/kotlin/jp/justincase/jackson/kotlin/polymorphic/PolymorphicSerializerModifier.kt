package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSuperclassOf

internal
class PolymorphicSerializerModifier : BeanSerializerModifier() {
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
    beanClass as KClass<in T>

    val superClasses = generateSequence(beanClass) {
      val t: Class<*>? = it.java.superclass

      // Super classes share the same lower bound
      @Suppress("UNCHECKED_CAST")
      t?.kotlin as? KClass<in T>
    }

    val modified = superClasses
        .mapNotNull { it.companionObjectInstance as? Polymorphic<*> }
        .firstOrNull()
        ?.let {
          // Prerequisite for `toTypeName`
          require(it.parameterType.isSuperclassOf(beanClass)) {
            "${it.parameterType} needs to be a super class of $beanClass"
          }

          // `it` shares the same lower bound with `beanClass`
          @Suppress("UNCHECKED_CAST")
          it as Polymorphic<in T>

          PolymorphicSerializer(it, serializer.unwrappingSerializer(null)::serialize)
        }

    return modified ?: serializer
  }
}
