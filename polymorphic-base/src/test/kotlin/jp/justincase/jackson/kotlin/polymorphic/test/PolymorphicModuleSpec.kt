package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.PolymorphicModule

sealed class Base {
  companion object : Polymorphic

  data class Impl(
      val prop1: String,
      val prop2: Int
  ) : Base()
}

class PolymorphicModuleSpec : StringSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  val impl = Base.Impl(
      prop1 = "a",
      prop2 = 1
  )
  val map = mapOf(
      "type" to "Impl",
      "prop1" to "a",
      "prop2" to 1
  )

  "writeValueAsString should output type name" {
    mapper.writeValueAsString(impl) shouldBe mapper.writeValueAsString(map)
  }
})
