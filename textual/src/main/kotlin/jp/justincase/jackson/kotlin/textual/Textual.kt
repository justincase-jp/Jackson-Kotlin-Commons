package jp.justincase.jackson.kotlin.textual

interface Textual<T : Any> : TextualSerializer<T>, TextualDeserializer<T>

interface TextualSerializer<in T : Any> {
  val T.text: String
}

interface TextualDeserializer<out T : Any> {
  fun fromText(text: String): T
}
