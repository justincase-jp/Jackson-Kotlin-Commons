package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.codec.PolymorphicModule

sealed class Result {
  companion object : Polymorphic {
    override val valueKey = "payload"
  }
}

data class Success<T>(val value: T) : Result()
data class Failure(val message: String) : Result()

private
fun main() {
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  println(mapper.writeValueAsString(Success(30))) // {"type":"Success","payload":{"value":30}}
  println(mapper.readValue<Result>("""{"type":"Failure","payload":{"message":"Unknown"}}""")) // Failure(message=Unknown)
}


class WrappedObjectExampleSpec : StringSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  "`main` should work" {
    main()
  }
  "Example output 1 should match the comment" {
    mapper.writeValueAsString(Success(30)) shouldBe """{"type":"Success","payload":{"value":30}}"""
  }
  "Example output 2 should match the comment" {
    mapper.readValue<Result>("""{"type":"Failure","payload":{"message":"Unknown"}}""").toString() shouldBe """Failure(message=Unknown)"""
  }
})
