package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.PolymorphicModule

data class BareImpl<T>(
    val prop1: T,
    val prop2: Int
) {
  companion object : Polymorphic // Consider making this an error?
}

sealed class LeakingBase {
  companion object : Polymorphic

  abstract class Impl : LeakingBase()

  data class ExtendedImpl(
      val prop1: String,
      val prop2: Int
  ) : Impl()
}

sealed class DisconnectedBaseBase {
  companion object : Polymorphic

  abstract class BaseImpl : DisconnectedBaseBase()

  sealed class DisconnectedBase : BaseImpl() {
    data class Impl(
        val prop1: String,
        val prop2: Int
    ) : DisconnectedBase()
  }
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

    "Leaking polymorphic type" should {
      val impl = LeakingBase.ExtendedImpl(
          prop1 = "a",
          prop2 = 1
      )
      val map = mapOf(
          "type" to "Impl",
          "prop1" to "a",
          "prop2" to 1
      )

      "output parent type name in `writeValueAsString`" {
        writeValueAsString(impl) shouldBe writeValueAsString(map)
      }
      "throw `InvalidDefinitionException` during deserialization" {
        shouldThrow<InvalidDefinitionException> {
          readValue<LeakingBase>(writeValueAsString(impl))
        }
      }
    }

    "Disconnected polymorphic type" should {
      val impl = DisconnectedBaseBase.DisconnectedBase.Impl(
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
        readValue<DisconnectedBaseBase.DisconnectedBase>(writeValueAsString(impl)) shouldBe impl
      }
      "throw `InvalidDefinitionException` during deserialization as disconnected parent type" {
        shouldThrow<InvalidDefinitionException> {
          readValue<DisconnectedBaseBase>(writeValueAsString(impl))
        }
      }
    }
  }
})
