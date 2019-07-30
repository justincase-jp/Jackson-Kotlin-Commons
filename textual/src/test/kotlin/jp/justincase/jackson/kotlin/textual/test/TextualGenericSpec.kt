package jp.justincase.jackson.kotlin.textual.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import jp.justincase.jackson.kotlin.textual.Textual
import jp.justincase.jackson.kotlin.textual.TextualModule

sealed class Option<out T> {
  companion object : Textual<Option<CharSequence>> {
    override
    val Option<CharSequence>.text
      get() = when (this) {
        None -> ""
        is Some -> "!$value"
      }

    override
    fun fromText(text: String) =
        when (text) {
          "" -> None
          else -> if (text.first() == '!') {
            Some(text.substring(1))
          } else {
            throw IllegalArgumentException(text)
          }
        }
  }
}

data class Some<out T>(val value: T) : Option<T>()

object None : Option<Nothing>()


class TextualGenericSpec : WordSpec({
  val mapper = ObjectMapper().registerModule(TextualModule())

  mapper.apply {
    "textual type with type parameter" should {
      "deserialize with the declaring type" {
        readValue<Option<CharSequence>>(writeValueAsString("")) shouldBe None
      }
      "deserialize with the exact type" {
        readValue<None>(writeValueAsString("")) shouldBe None
      }
      "work with serialization" {
        writeValueAsString(Some("")) shouldBe writeValueAsString("!")
        writeValueAsString(None) shouldBe writeValueAsString("")
      }
    }
  }
})
