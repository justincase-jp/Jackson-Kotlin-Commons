@file:Suppress("UnstableApiUsage")
package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.common.reflect.TypeToken
import java.lang.reflect.*
import kotlin.reflect.KClass
import kotlin.reflect.KVariance
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
fun JavaType.toWildcardType(variance: KVariance): Type =
    when (variance) {
      KVariance.INVARIANT -> toType()
      KVariance.IN -> object : WildcardType {
        override
        fun getUpperBounds(): Array<Type> =
            emptyArray()

        override
        fun getLowerBounds(): Array<Type> =
            arrayOf(toType())

        override
        fun toString(): String =
            toCanonical()
      }
      KVariance.OUT -> object : WildcardType {
        override
        fun getUpperBounds(): Array<Type> =
            arrayOf(toType())

        override
        fun getLowerBounds(): Array<Type> =
            emptyArray()

        override
        fun toString(): String =
            toCanonical()
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
              toCanonical()
        }
      else ->
        object : ParameterizedType {
          override
          fun getRawType(): Type =
              rawClass

          override
          fun getActualTypeArguments(): Array<Type> =
              rawClass
                  .kotlin
                  .typeParameters
                  .map { it.variance }
                  .zip(bindings.typeParameters)
                  .map { (v, p) -> p.toWildcardType(v) }
                  .toTypedArray()

          override
          fun getOwnerType(): Type? =
              null

          override
          fun toString(): String =
              toCanonical()
        }
    }

internal
fun JavaType.toTypeToken() =
    TypeToken.of(toType())

internal
fun TypeToken<*>.toJavaType(factory: TypeFactory) =
    factory.constructType(type)
