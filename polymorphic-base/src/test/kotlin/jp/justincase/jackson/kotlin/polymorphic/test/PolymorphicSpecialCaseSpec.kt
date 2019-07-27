package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.PolymorphicModule

data class BareImpl<T>(
    val prop1: T,
    val prop2: Int
) {
  companion object : Polymorphic // Consider making this an error?
}


class PolymorphicSpecialCaseSpec : WordSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  mapper.apply {
    "Bare data class" should {
      val impl = BareImpl(
          prop1 = listOf("a"),
          prop2 = 1
      )
      val map = mapOf(
          "prop1" to listOf("a"),
          "prop2" to 1
      )

      "NOT output type name in `writeValueAsString`" {
        writeValueAsString(impl) shouldBe writeValueAsString(map)
      }
      "work with round trip" {
        readValue<BareImpl<List<String>>>(writeValueAsString(impl)) shouldBe impl
      }
    }
  }
})
