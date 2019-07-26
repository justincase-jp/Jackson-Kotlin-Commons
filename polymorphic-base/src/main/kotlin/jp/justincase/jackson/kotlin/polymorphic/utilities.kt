@file:Suppress("UnstableApiUsage")
package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.common.reflect.TypeToken
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

internal
val <T : Any> KClass<T>.allNonInterfaceSuperclasses: Sequence<KClass<in T>>
  get() = generateSequence<KClass<in T>>(this) {
    val t: Class<*>? = it.java.superclass

    // Super classes share the same lower bound
    @Suppress("UNCHECKED_CAST")
    t?.kotlin as? KClass<in T>
  }

internal
fun <T : Any> KClass<T>.leafClassPolymorphicInstances(root: Polymorphic?): Sequence<Pair<KClass<out T>, Polymorphic?>> =
  sequence {
    for (t in sealedSubclasses) {
      val p = t.companionObjectInstance as? Polymorphic ?: root

      if (!t.isSealed) {
        yield(t to p)
      } else {
        yieldAll(t.leafClassPolymorphicInstances(p))
      }
    }
  }

private
fun JavaType.toType(): Type =
    when {
      bindings.isEmpty ->
        rawClass
      isArrayType ->
        object : GenericArrayType {
          override
          fun getGenericComponentType(): Type? =
              contentType.toType()

          override
          fun toString(): String =
              this@toType.toCanonical()
        }
      else ->
        object : ParameterizedType {
          override
          fun getRawType(): Type =
              rawClass

          override
          fun getActualTypeArguments(): Array<Type> =
              bindings.typeParameters.map { it.toType() }.toTypedArray()

          override
          fun getOwnerType(): Type? =
              null

          override
          fun toString(): String =
              this@toType.toCanonical()
        }
    }

internal
fun JavaType.toTypeToken() =
    TypeToken.of(toType())

internal
fun TypeToken<*>.toJavaType(factory: TypeFactory) =
    factory.constructType(type)
