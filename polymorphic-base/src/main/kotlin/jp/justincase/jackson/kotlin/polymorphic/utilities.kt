@file:Suppress("UnstableApiUsage")
package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.common.reflect.TypeToken
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

internal
fun JavaType.toTypeToken() =
    TypeToken.of(object : ParameterizedType {
      override
      fun getRawType(): Type =
          rawClass

      override
      fun getActualTypeArguments(): Array<Type> =
          bindings.typeParameters.toTypedArray()

      override
      fun getOwnerType(): Type? =
          null
    })

internal
fun TypeToken<*>.toJavaType(factory: TypeFactory) =
    factory.constructType(type)
