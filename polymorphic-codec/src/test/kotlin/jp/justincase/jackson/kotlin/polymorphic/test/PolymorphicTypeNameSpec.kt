package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.codec.PolymorphicModule
import kotlin.reflect.KClass

sealed class IllegalTypeNameBase {
  companion object : Polymorphic {
    override
    val KClass<out Any>.typeName: String
      get() = throw IllegalArgumentException(toString())
  }

  data class Impl(
      val prop1: String,
      val prop2: Int
  ) : IllegalTypeNameBase()
}

sealed class DuplicateTypeNameBase {
  companion object : Polymorphic

  data class Impl(
      val prop1: String,
      val prop2: Int
  ) : DuplicateTypeNameBase() {
    @Suppress("unused")
    data class Impl(
        val prop1: String,
        val prop2: Int
    ) : DuplicateTypeNameBase()
  }
}

sealed class AmbiguousMappingBase {
  companion object : Polymorphic

  data class Impl(
      val prop1: String,
      val prop2: Int
  ) : AmbiguousMappingBase()

  data class Impl1(
      val prop1: String,
      val prop2: Int
  ) : AmbiguousMappingBase() {
    companion object : Polymorphic {
      override val typeKey: String
        get() = "type1"
    }
  }
}


class PolymorphicTypeNameSpec : WordSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  mapper.apply {
    "Polymorphic type that throw `IllegalArgumentException` on `::toTypeName`" should {
      val impl = IllegalTypeNameBase.Impl(
          prop1 = "a",
          prop2 = 1
      )

      "throw `JsonMappingException` in `writeValueAsString`" {
        shouldThrow<JsonMappingException> {
          writeValueAsString(impl)
        }
      }
      "throw `JsonMappingException` in `readValue`" {
        shouldThrow<JsonMappingException> {
          readValue<IllegalTypeNameBase>("")
        }
      }
    }

    "Polymorphic type that has duplicate type name" should {
      val impl = DuplicateTypeNameBase.Impl(
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
      "throw `JsonMappingException` with round trip" {
        shouldThrow<JsonMappingException> {
          readValue<DuplicateTypeNameBase>(writeValueAsString(impl))
        }
      }
    }

    "Polymorphic type that has ambiguous mapping" should {
      val map = mapOf(
          "type" to "Impl",
          "type1" to "Impl1"
      )

      "throw `MismatchedInputException` with the ambiguous mapping" {
        shouldThrow<MismatchedInputException> {
          readValue<AmbiguousMappingBase>(writeValueAsString(map))
        }
      }
    }
  }
})
