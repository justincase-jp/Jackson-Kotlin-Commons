package jp.justincase.jackson.kotlin.polymorphic

import kotlin.reflect.KClass

internal
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
