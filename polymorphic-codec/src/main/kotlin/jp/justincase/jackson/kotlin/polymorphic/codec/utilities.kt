@file:Suppress("UnstableApiUsage")
package jp.justincase.jackson.kotlin.polymorphic.codec

import jp.justincase.jackson.kotlin.internal.effectiveCompanion
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import kotlin.reflect.KClass

internal
fun <T : Any> KClass<T>.leafClassPolymorphicInstances(root: Polymorphic?): Sequence<Pair<KClass<out T>, Polymorphic?>> =
  sequence {
    for (t in sealedSubclasses) {
      val p = t.effectiveCompanion as? Polymorphic ?: root

      if (!t.isSealed) {
        yield(t to p)
      } else {
        yieldAll(t.leafClassPolymorphicInstances(p))
      }
    }
  }


internal
fun <T, R> constant(value: R): (T) -> R =
    { value }
