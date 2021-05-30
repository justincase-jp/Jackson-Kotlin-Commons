@file:Suppress("UnstableApiUsage")
package jp.justincase.jackson.kotlin.internal

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.reflect.TypeToken
import kotlin.reflect.KClass

private
val superInterfaceCache =
    CacheBuilder
        .newBuilder()
        .weakKeys()
        .build(CacheLoader.from { t: Class<out Any>? ->
            TypeToken.of(t).types.interfaces().map { it.rawType.kotlin }
        })

val KClass<out Any>.allNonInterfaceSuperclassesAndInterfaces
  get() = sequence {
    yieldAll(allNonInterfaceSuperclasses)
    yieldAll(superInterfaceCache.get(java))
  }
