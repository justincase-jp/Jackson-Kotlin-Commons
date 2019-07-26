package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.PolymorphicModule

sealed class GenericBase<String> {
  companion object : Polymorphic

  data class Impl<T>(
      val prop1: T,
      val prop2: Int
  ) : GenericBase<T>()
}

class PolymorphicModuleGenericSpec : WordSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  mapper.apply {
    "Generic polymorphic type" should {
      val impl = GenericBase.Impl(
          prop1 = "a",
          prop2 = 1
      )
      val map = mapOf(
          "type" to "Impl",
          "prop1" to "a",
          "prop2" to 1
      )

      "output type name in `writeValueAsString`" {
        writeValueAsString(impl) shouldBe writeValueAsString(map)
      }
      "work with Round trip" {
        readValue<GenericBase<String>>(writeValueAsString(impl)) shouldBe impl
      }
    }
  }
})
