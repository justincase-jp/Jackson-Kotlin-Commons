@file:Suppress("BlockingMethodInNonBlockingContext")
package jp.justincase.jackson.kotlin.textual.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import jp.justincase.jackson.kotlin.textual.Textual
import jp.justincase.jackson.kotlin.textual.codec.TextualModule

sealed class Option<out T> {
  companion object : Textual<Option<CharSequence>> {
    override
    val Option<CharSequence>.text
      get() = when (this) {
        None -> ""
        is Some -> "!$value"
      }

    override
    fun fromText(value: String) =
        when (value) {
          "" -> None
          else -> if (value.first() == '!') {
            Some(value.substring(1))
          } else {
            throw IllegalArgumentException(value)
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
