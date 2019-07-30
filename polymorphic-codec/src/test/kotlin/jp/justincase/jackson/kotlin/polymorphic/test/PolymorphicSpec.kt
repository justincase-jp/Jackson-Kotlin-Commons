package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.codec.PolymorphicModule

sealed class Base {
  companion object : Polymorphic

  data class Impl(
      val prop1: String,
      val prop2: Int
  ) : Base()
}

sealed class WrappedBase {
  companion object : Polymorphic {
    override
    val valueKey = "value"
  }

  data class Impl(
      val prop1: String,
      val prop2: Int
  ) : WrappedBase()
}

sealed class ObjectBase<T> {
  companion object : Polymorphic

  object Impl : ObjectBase<String>()
}

sealed class NonPolymorphicBase {
  data class Impl(
      val prop1: String,
      val prop2: Int
  ) : NonPolymorphicBase()
}


class PolymorphicSpec : WordSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  mapper.apply {
    "Flat polymorphic type" should {
      val impl = Base.Impl(
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
      "work with round trip" {
        readValue<Base>(writeValueAsString(impl)) shouldBe impl
      }
      "throw `MismatchedInputException` for reading value with non-object" {
        shouldThrow<MismatchedInputException> {
          readValue<Base>(writeValueAsString(listOf("a")))
        }
      }
    }

    "Wrapped polymorphic type" should {
      val impl = WrappedBase.Impl(
          prop1 = "a",
          prop2 = 1
      )
      val map = mapOf(
          "type" to "Impl",
          "value" to mapOf(
              "prop1" to "a",
              "prop2" to 1
          )
      )

      "output type name in `writeValueAsString`" {
        writeValueAsString(impl) shouldBe writeValueAsString(map)
      }
      "work with Round trip" {
        readValue<WrappedBase>(writeValueAsString(impl)) shouldBe impl
      }
    }

    "Polymorphic object type" should {
      val impl = ObjectBase.Impl
      val map = mapOf(
          "type" to "Impl"
      )

      "output type name in `writeValueAsString`" {
        writeValueAsString(impl) shouldBe writeValueAsString(map)
      }
      "work with round trip" {
        readValue<ObjectBase<String>>(writeValueAsString(impl)) shouldBe impl
      }
      "throw `MismatchedInputException` if incompatible type parameter is used in `readValue`" {
        shouldThrow<MismatchedInputException> {
          readValue<ObjectBase<Int>>(writeValueAsString(impl))
        }
      }
    }

    "Non-polymorphic type" should {
      val impl = NonPolymorphicBase.Impl(
          prop1 = "a",
          prop2 = 1
      )
      val map = mapOf(
          "prop1" to "a",
          "prop2" to 1
      )

      "NOT output type name in `writeValueAsString`" {
        writeValueAsString(impl) shouldBe writeValueAsString(map)
      }
      "work with round trip (when the actual type is supplied directly)" {
        readValue<NonPolymorphicBase.Impl>(writeValueAsString(impl)) shouldBe impl
      }
    }
  }
})
