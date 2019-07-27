package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.PolymorphicModule
import kotlin.reflect.KClass


sealed class IllegalTypeNameBase {
  companion object : Polymorphic {
    override
    val KClass<out Any>.toTypeName: String
      get() = throw IllegalArgumentException(toString())
  }

  data class Impl(
      val prop1: String,
      val prop2: Int
  ) : IllegalTypeNameBase()
}


class PolymorphicExceptionSpec : WordSpec({
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
  }
})
