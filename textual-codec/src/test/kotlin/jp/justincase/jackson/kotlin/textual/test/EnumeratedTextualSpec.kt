@file:Suppress("BlockingMethodInNonBlockingContext")
package jp.justincase.jackson.kotlin.textual.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jp.justincase.jackson.kotlin.enumerated.Enumerated
import jp.justincase.jackson.kotlin.textual.codec.TextualModule

sealed class Base {
  object A : Base()
  object B : Base()
  object C : Base()

  companion object : Enumerated<Base>
}


class EnumeratedTextualSpec : StringSpec({
  val mapper = ObjectMapper().registerModule(TextualModule())

  mapper.apply {
    "enumerated type deserialization should work" {
      readValue<Base>(writeValueAsString("B")) shouldBe Base.B
    }
    "enumerated type deserialization from null should work" {
      readValue<Base>(writeValueAsString(null)) shouldBe null
    }
    "enumerated type serialization should work" {
      writeValueAsString(Base.C) shouldBe writeValueAsString("C")
    }
  }
})
