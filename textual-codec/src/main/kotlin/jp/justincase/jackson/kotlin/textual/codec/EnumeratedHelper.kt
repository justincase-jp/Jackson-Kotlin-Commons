package jp.justincase.jackson.kotlin.textual.codec

import jp.justincase.jackson.kotlin.enumerated.Enumerated
import jp.justincase.jackson.kotlin.enumerated.get
import jp.justincase.jackson.kotlin.textual.TextualDecoder
import jp.justincase.jackson.kotlin.textual.TextualEncoder

internal
val Any.enumeratedAsTextualEncoder: TextualEncoder<Nothing>?
  get() = (this as? Enumerated<*>)?.let {
    // Workaround for use of wildcard type against upper bound
    @Suppress("UNCHECKED_CAST")
    (this as Enumerated<Any>).textualEncoder
  }

private
val <T : Any> Enumerated<T>.textualEncoder
  get() = object : TextualEncoder<T> {
    override
    val T.text: String
      get() = name
  }


internal
val Any.enumeratedAsTextualDecoder: TextualDecoder<Any>?
  get() = (this as? Enumerated<*>)?.let {
    object : TextualDecoder<Any> {
      override
      fun fromText(value: String): Any =
          it[value] ?: throw IllegalArgumentException(value)
    }
  }
