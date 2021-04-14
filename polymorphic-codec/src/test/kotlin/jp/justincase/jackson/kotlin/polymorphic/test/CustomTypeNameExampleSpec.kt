package jp.justincase.jackson.kotlin.polymorphic.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jp.justincase.jackson.kotlin.polymorphic.Polymorphic
import jp.justincase.jackson.kotlin.polymorphic.codec.PolymorphicModule
import kotlin.reflect.KClass

sealed class Identity {
  companion object : Polymorphic {
    override val typeKey = "role"

    override val KClass<out Any>.typeName
      get() = simpleName?.toLowerCase() ?: throw IllegalArgumentException(toString())
  }
}

data class User(val id: String) : Identity()
data class Admin(val id: String) : Identity()

private
fun main() {
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  println(mapper.writeValueAsString(User("A"))) // {"role":"user","id":"A"}
  println(mapper.readValue<Identity>("""{"role":"admin","id":"B"}""")) // Admin(id=B)
}


class CustomTypeKeyExampleSpec : StringSpec({
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  "`main` should work" {
    main()
  }
  "Example output 1 should match the comment" {
    mapper.writeValueAsString(User("A")) shouldBe """{"role":"user","id":"A"}"""
  }
  "Example output 2 should match the comment" {
    mapper.readValue<Identity>("""{"role":"admin","id":"B"}""").toString() shouldBe """Admin(id=B)"""
  }
})
