@file:Suppress("UnstableApiUsage")
package jp.justincase.jackson.kotlin.polymorphic

import com.google.common.cache.CacheBuilder
import com.google.common.reflect.TypeToken
import kotlin.reflect.KClass

private
val typeMappings =
    CacheBuilder.newBuilder().weakKeys().build<Polymorphic<*>, Map<String, KClass<*>>>().asMap()

internal
val <T : Any> Polymorphic<T>.parameterType: KClass<T>
  get() {
    val t: Class<*> = TypeToken
        .of(javaClass)
        .resolveType(Polymorphic::class.java.typeParameters.first())
        .rawType

    // The first type parameter of `this` is `T`
    @Suppress("UNCHECKED_CAST")
    return t.kotlin as KClass<T>
  }

interface Polymorphic<T : Any> {
  val typeKey
    get() = "type"

  val KClass<out T>.toTypeName: String
    get() = simpleName ?: throw IllegalArgumentException(toString())

  val String.toType: KClass<out T>
    get() {
      val m = typeMappings.getOrPut(this@Polymorphic) {
        parameterType
            .leafClasses
            .fold(mutableMapOf<String, KClass<out T>>()) { m, t ->
              m.put(t.toTypeName, t)?.let {
                throw IllegalArgumentException("Duplicate type names: $it and $t")
              }
              m
            }
      }

      // Type parameter `T` is dropped during insertion
      @Suppress("UNCHECKED_CAST")
      m as Map<String, KClass<out T>>

      return m[this] ?: throw IllegalArgumentException(this)
    }
}
