package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.PolymorphicModule

sealed class Identity {
  companion object : Polymorphic {
    override val typeKey = "role"
  }
}

data class User(val id: String) : Identity()
data class Admin(val id: String) : Identity()

private
fun main() {
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  println(mapper.writeValueAsString(User("A"))) // {"role":"User","id":"A"}
  println(mapper.writeValueAsString(Admin("B"))) // {"role":"Admin","id":"B"}
}

class CustomizationExampleSpec : StringSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  """`main` should work""" {
    main()
  }
  "Example output 1 should match the comment" {
    mapper.writeValueAsString(User("A")) shouldBe """{"role":"User","id":"A"}"""
  }
  "Example output 2 should match the comment" {
    mapper.writeValueAsString(Admin("B")) shouldBe """{"role":"Admin","id":"B"}"""
  }
})
