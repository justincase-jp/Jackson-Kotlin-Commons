package jp.justincase.jackson.kotlin.cause

import jp.justincase.jackson.kotlin.internal.internalDetachedCause

val Throwable.detachedCause: Throwable?
  get() = internalDetachedCause
