package jp.justincase.jackson.kotlin.internal

import com.google.common.cache.CacheBuilder

private
val map =
    CacheBuilder.newBuilder().weakKeys().build<Throwable, Throwable>().asMap()

var Throwable.internalDetachedCause: Throwable?
  get() = map[this]
  set(value) {
    when (value) {
      null -> map.remove(this)
      else -> map[this] = value
    }
  }
