package jp.justincase.jackson.kotlin.enumerated

interface Enumerated<V : Any> {
  val V.name: String
    get() = this::class.simpleName ?: throw IllegalArgumentException(toString())
}

val <V : Any> Enumerated<V>.values: Set<V>
  get() = backing.values

operator fun <V : Any> Enumerated<V>.get(name: String): V? =
    backing[name]
