package jp.justincase.jackson.kotlin.textual

interface Textual<T : Any> : TextualEncoder<T>, TextualDecoder<T>

interface TextualEncoder<in T : Any> {
  val T.text: String
}

interface TextualDecoder<out T : Any> {
  fun fromText(value: String): T

  fun fromNull(): T? = null
}
