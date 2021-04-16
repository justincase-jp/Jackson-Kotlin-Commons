package jp.justincase.jackson.kotlin.enumerated.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import jp.justincase.jackson.kotlin.enumerated.Enumerated
import jp.justincase.jackson.kotlin.enumerated.values

sealed class Base {
  object A : Base()
  object B : Base()
  object C : Base()

  companion object : Enumerated<Base>
}


class EnumeratedSpec : StringSpec({
  "`Enumerated::values` should work" should {
    Base.values shouldBe setOf(Base.A, Base.B, Base.C)
  }
})
