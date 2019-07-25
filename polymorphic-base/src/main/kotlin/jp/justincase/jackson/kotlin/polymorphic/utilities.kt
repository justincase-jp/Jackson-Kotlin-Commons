package jp.justincase.jackson.kotlin.polymorphic

import kotlin.reflect.KClass

internal
val <T : Any> KClass<T>.allNonInterfaceSuperclasses: Sequence<KClass<in T>>
  get() = generateSequence<KClass<in T>>(this) {
    val t: Class<*>? = it.java.superclass

    // Super classes share the same lower bound
    @Suppress("UNCHECKED_CAST")
    t?.kotlin as? KClass<in T>
  }
