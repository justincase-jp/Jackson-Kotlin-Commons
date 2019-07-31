package jp.justincase.jackson.kotlin.internal

import com.fasterxml.jackson.core.JsonProcessingException
import com.google.common.cache.CacheBuilder

private
val map =
    CacheBuilder
        .newBuilder()
        .weakKeys()
        .build<JsonProcessingException, Throwable>()
        .asMap()

var JsonProcessingException.internalDetachedCause: Throwable?
  get() = map[this]
  set(value) {
    when (value) {
      null -> map.remove(this)
      else -> map[this] = value
    }
  }
