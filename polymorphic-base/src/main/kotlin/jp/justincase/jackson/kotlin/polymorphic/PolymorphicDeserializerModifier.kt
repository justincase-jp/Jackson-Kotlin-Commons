package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer
import com.google.common.collect.HashBasedTable
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

internal
object PolymorphicDeserializerModifier : BeanDeserializerModifier() {
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
  ): JsonDeserializer<out T> {
    val beanClass = beanDesc.beanClass.kotlin

    // Assume the caller is correct
    @Suppress("UNCHECKED_CAST")
    beanClass as KClass<T>

    val polymorphic = beanClass
        .allNonInterfaceSuperclasses
        .mapNotNull { it.companionObjectInstance as? Polymorphic }
        .firstOrNull()
    val typeTable = polymorphic
        .let { beanClass.leafClassPolymorphicInstances(it) }
        .fold(HashBasedTable.create<String, String, Pair<KClass<out T>, String?>>()) { table, it ->
          val (t, p) = it

          p?.apply {
            table.put(typeKey, t.toTypeName, t to valueKey)?.let {
              throw IllegalArgumentException("Duplicate type names: ${it.first} and $t")
            }
          }
          table
        }

    return if (!typeTable.isEmpty) {
      PolymorphicDeserializer(
          typeTable.rowMap(),
          beanClass,
          deserializer::deserialize
      )
    } else {
      val wrapper = polymorphic
          ?.takeIf { beanClass.companionObjectInstance != it } // Ignore 'self-polymorphic' type
          ?.run {
            PolymorphicDirectDeserializer(
                typeKey,
                beanClass.toTypeName,
                valueKey,
                deserializer::deserialize,
                if (deserializer is ResolvableDeserializer) {
                  val resolvable: ResolvableDeserializer = deserializer

                  { resolvable.resolve(it) } // Aid type inference by using lambda
                } else {
                  constant(Unit)
                }
            )
          }

      wrapper ?: deserializer
    }
  }
}
