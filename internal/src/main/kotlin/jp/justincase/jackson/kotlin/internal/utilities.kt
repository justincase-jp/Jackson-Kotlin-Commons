@file:Suppress("UnstableApiUsage")
package jp.justincase.jackson.kotlin.internal

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.common.reflect.TypeToken
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.reflect.KClass
import kotlin.reflect.KVariance

val <T : Any> KClass<in T>.allNonInterfaceSuperclasses: Sequence<KClass<in T>>
  get() = generateSequence<Class<*>>(java, Class<*>::getSuperclass).map {
    // Super classes share the same lower bound
    @Suppress("UNCHECKED_CAST")
    it.kotlin as KClass<in T>
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
                  .zip(bindings.typeParameters) { baseType, bindingType ->
                    bindingType.toWildcardType(baseType.variance)
                  }
                  .toTypedArray()

          override
          fun getOwnerType(): Type? =
              null

          override
          fun toString(): String =
              toCanonical()
        }
    }

fun JavaType.toTypeToken(): TypeToken<*> =
    TypeToken.of(toType())

fun TypeToken<*>.toJavaType(factory: TypeFactory): JavaType =
    factory.constructType(type)