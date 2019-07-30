package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.PolymorphicModule

sealed class GenericBase<T> {
  companion object : Polymorphic

  data class Impl<T>(
      val prop1: T,
      val prop2: Int
  ) : GenericBase<List<T>>()
}


class PolymorphicGenericsSpec : WordSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  mapper.apply {
    "Generic polymorphic type" should {
      val impl = GenericBase.Impl(
          prop1 = listOf("a"),
          prop2 = 1
      )
      val map = mapOf(
          "type" to "Impl",
          "prop1" to listOf("a"),
          "prop2" to 1
      )

      "output type name in `writeValueAsString`" {
        writeValueAsString(impl) shouldBe writeValueAsString(map)
      }
      "work with round trip" {
        readValue<GenericBase<List<List<String>>>>(writeValueAsString(impl)) shouldBe impl
      }
      "work with exact type deserialization" {
        readValue<GenericBase.Impl<List<String>>>(writeValueAsString(impl)) shouldBe impl
      }
    }
  }
})
