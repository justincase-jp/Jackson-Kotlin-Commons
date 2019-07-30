package jp.justincase.jackson.kotlin.textual.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import jp.justincase.jackson.kotlin.textual.Textual
import jp.justincase.jackson.kotlin.textual.TextualModule

data class Hexadecimal(val value: Int) {
  companion object : Textual<Hexadecimal> {
    override
    val Hexadecimal.text
      get() = value.toString(16)

    override
    fun fromText(text: String) =
        Hexadecimal(text.toInt(16))
  }
}

class TextualSpec : StringSpec({
  val mapper = ObjectMapper().registerModule(TextualModule())

  mapper.apply {
    "textual type deserialization should work" {
      readValue<Hexadecimal>(writeValueAsString("a")) shouldBe Hexadecimal(10)
    }
    "textual type serialization should work" {
      writeValueAsString(Hexadecimal(15)) shouldBe writeValueAsString("f")
    }
  }
})
