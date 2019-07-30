package jp.justincase.jackson.kotlin.textual.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import jp.justincase.jackson.kotlin.textual.Textual
import jp.justincase.jackson.kotlin.textual.TextualModule

data class Hexadecimal(val value: Int) {
  companion object : Textual<Hexadecimal> {
    override val Hexadecimal.text
      get() = value.toString(16)

    override fun fromText(value: String) =
        Hexadecimal(value.toInt(16))
  }
}

private
fun main() {
  val mapper = ObjectMapper().registerModule(TextualModule())

  println(mapper.writeValueAsString(Hexadecimal(1000))) // "3e8"
  println(mapper.readValue<Hexadecimal>(""""2a"""")) // Hexadecimal(value=42)
}


class ExampleSpec: StringSpec({
  val mapper = ObjectMapper().registerModule(TextualModule())

  "`main` should work" {
    main()
  }
  "Example output 1 should match the comment" {
    mapper.writeValueAsString(Hexadecimal(1000)) shouldBe """"3e8""""
  }
  "Example output 2 should match the comment" {
    mapper.readValue<Hexadecimal>(""""2a"""").toString() shouldBe """Hexadecimal(value=42)"""
  }
})
