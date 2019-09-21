package jp.justincase.jackson.kotlin.enumerated

import com.google.common.cache.CacheBuilder
import com.google.common.collect.BiMap
import com.google.common.collect.ImmutableBiMap
import kotlin.reflect.KClass

private
val <T : Any> KClass<T>.leafClasses: Sequence<KClass<out T>>
  get() = sequence {
    for (t in sealedSubclasses) {
      if (!t.isSealed) {
        yield(t)
      } else {
        yieldAll(t.leafClasses)
      }
    }
  }

private
val Any.companionHost
  get() = this::class
      .takeIf(KClass<out Any>::isCompanion)
      ?.java
      ?.declaringClass
      ?.kotlin


private
val backingMaps: MutableMap<Enumerated<*>, BiMap<String, out Any>> =
    CacheBuilder
        .newBuilder()
        .weakKeys()
        .build<Enumerated<*>, BiMap<String, out Any>>()
        .asMap()

private
val <V : Any> Enumerated<V>.initializedBacking: BiMap<String, out V>?
  @Suppress("UNCHECKED_CAST")
  get() = backingMaps[this] as BiMap<String, V>?

internal
val <V : Any> Enumerated<V>.backing: BiMap<String, out V>
  get() = initializedBacking ?: ImmutableBiMap
      .builder<String, V>()
      .also { map ->
        requireNotNull(companionHost)
            .leafClasses
            .forEach { kClass ->
              val v = requireNotNull(kClass.objectInstance)

              // TODO: Check against the type parameter at `Enumerated`
              @Suppress("UNCHECKED_CAST")
              v as V
              map.put(v.name, v)
            }
      }
      .build()
      .also {
        backingMaps[this] = it
      }
