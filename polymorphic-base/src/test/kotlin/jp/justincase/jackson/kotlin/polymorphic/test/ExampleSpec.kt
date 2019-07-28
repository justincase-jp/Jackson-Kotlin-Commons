package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.specs.WordSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.PolymorphicModule

sealed class Option<out T> {
  companion object : Polymorphic // Implement `Polymorphic` to handle this as a polymorphic type
}

data class Some<out T>(val value: T) : Option<T>()

object None : Option<Nothing>() {
  override fun toString() = "None"
}


data class Foo(val bar: Boolean)

private
fun main() {
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  println(mapper.writeValueAsString(Some(30))) // {"type":"Some","value":30}
  println(mapper.writeValueAsString(Some(Foo(true)))) // {"type":"Some","value":{"bar":true}}
  println(mapper.writeValueAsString(None)) // {"type":"None"}

  println(mapper.readValue<Option<String>>("""{"type":"Some","value":"abc"}""")) // Some(value=abc)
  println(mapper.readValue<Option<Foo>>("""{"type":"Some","value":{"bar":true}}""")) // Some(value=Foo(bar=true))
  println(mapper.readValue<Option<String>>("""{"type":"None"}""")) // None
}


class ExampleSpec : StringSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  """`main` should work""" {
    main()
  }
  "Example output 1 should match the comment" {
    mapper.writeValueAsString(Some(30)) shouldBe """{"type":"Some","value":30}"""
  }
  "Example output 2 should match the comment" {
    mapper.writeValueAsString(Some(Foo(true))) shouldBe """{"type":"Some","value":{"bar":true}}"""
  }
  "Example output 3 should match the comment" {
    mapper.writeValueAsString(None) shouldBe """{"type":"None"}"""
  }
  "Example output 4 should match the comment" {
    mapper.readValue<Option<String>>("""{"type":"Some","value":"abc"}""").toString() shouldBe """Some(value=abc)"""
  }
  "Example output 5 should match the comment" {
    mapper.readValue<Option<Foo>>("""{"type":"Some","value":{"bar":true}}""").toString() shouldBe """Some(value=Foo(bar=true))"""
  }
  "Example output 6 should match the comment" {
    mapper.readValue<Option<String>>("""{"type":"None"}""").toString() shouldBe """None"""
  }
})
