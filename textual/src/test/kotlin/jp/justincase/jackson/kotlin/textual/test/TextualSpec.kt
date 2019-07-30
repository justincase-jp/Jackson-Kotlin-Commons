package jp.justincase.jackson.kotlin.textual.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import jp.justincase.jackson.kotlin.textual.Textual
import jp.justincase.jackson.kotlin.textual.codec.TextualModule
import java.lang.IllegalArgumentException

data class MyUnit(
    val value: Unit = Unit
) {
  companion object : Textual<MyUnit> {
    override
    val MyUnit.text: String
      get() = "()"

    override
    fun fromText(value: String): MyUnit =
        when (value) {
          "()" -> MyUnit()
          else -> throw IllegalArgumentException(value)
        }
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

    "textual type deserialization should convert `IllegalArgumentException` to `MismatchedInputException`" {
      shouldThrow<MismatchedInputException> {
        readValue<MyUnit>(writeValueAsString("a"))
      }
    }
  }
})
