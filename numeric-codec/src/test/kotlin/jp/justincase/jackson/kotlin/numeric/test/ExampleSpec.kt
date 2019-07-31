package jp.justincase.jackson.kotlin.numeric.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import jp.justincase.jackson.kotlin.numeric.Numeric
import jp.justincase.jackson.kotlin.numeric.codec.NumericModule
import java.math.BigDecimal

data class Natural(val value: BigDecimal) {
  init {
    require(value >= BigDecimal.ZERO)
  }
  constructor(value: Int) : this(BigDecimal(value))

  companion object : Numeric<Natural> {
    override val Natural.decimal
      get() = value

    override fun fromDecimal(value: BigDecimal) =
        Natural(value)
  }
}

private
fun main() {
  val mapper = ObjectMapper().registerModule(NumericModule())

  println(mapper.writeValueAsString(Natural(1000))) // 1000
  println(mapper.readValue<Natural>("1000")) // Natural(value=1000)

  mapper.readValue<Natural>("-50") // throws MismatchedInputException
}


class ExampleSpec: StringSpec({
  val mapper = ObjectMapper().registerModule(NumericModule())

  "`main` should throws `MismatchedInputException`" {
    shouldThrow<MismatchedInputException> {
      main()
    }
  }
  "Example output 1 should match the comment" {
    mapper.writeValueAsString(Natural(1000)) shouldBe "1000"
  }
  "Example output 2 should match the comment" {
    mapper.readValue<Natural>("1000").toString() shouldBe "Natural(value=1000)"
  }
})
