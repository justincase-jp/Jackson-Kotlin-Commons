package jp.justincase.jackson.kotlin.cause

import com.fasterxml.jackson.core.JsonProcessingException
import jp.justincase.jackson.kotlin.internal.internalDetachedCause

val JsonProcessingException.detachedCause: Throwable?
  get() = internalDetachedCause
