[![Release](https://jitpack.io/v/io.github.justincase-jp/jackson-kotlin-commons.svg)](
  https://jitpack.io/#io.github.justincase-jp/jackson-kotlin-commons
)
[![Build Status](https://circleci.com/gh/justincase-jp/Jackson-Kotlin-Commons/tree/master.svg?style=shield)](
  https://circleci.com/gh/justincase-jp/Jackson-Kotlin-Commons
)

Jackson Kotlin Commons
===
Pluggable Kotlin utilities for type-safe JSON encoding with Jackson.

## Polymorphic
Sealed-class based polymorphic type JSON encoding.

### Basic usage

```kotlin
sealed class Option<out T> {
  companion object : Polymorphic // Implement `Polymorphic` to handle this as a polymorphic type
}

data class Some<out T>(val value: T) : Option<T>()

object None : Option<Nothing>() {
  override fun toString() = "None"
}


data class Foo(val bar: Boolean)

fun main() {
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  println(mapper.writeValueAsString(Some(30))) // {"type":"Some","value":30}
  println(mapper.writeValueAsString(Some(Foo(true)))) // {"type":"Some","value":{"bar":true}}
  println(mapper.writeValueAsString(None)) // {"type":"None"}

  println(mapper.readValue<Option<String>>("""{"type":"Some","value":"abc"}""")) // Some(value=abc)
  println(mapper.readValue<Option<Foo>>("""{"type":"Some","value":{"bar":true}}""")) // Some(value=Foo(bar=true))
  println(mapper.readValue<Option<String>>("""{"type":"None"}""")) // None
}
```

### Installation

Gradle Kotlin DSL

```kotlin
repositories {
  maven("https://jitpack.io")
}
dependencies {
  implementation("io.github.justincase-jp.jackson-kotlin-commons", "polymorphic", VERSION)
}
```

### Customization

##### Custom type name

```kotlin
sealed class Identity {
  companion object : Polymorphic {
    override val typeKey = "role"

    override val KClass<out Any>.typeName
      get() = simpleName?.toLowerCase() ?: throw IllegalArgumentException(toString())
  }
}

data class User(val id: String) : Identity()
data class Admin(val id: String) : Identity()

fun main() {
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  println(mapper.writeValueAsString(User("A"))) // {"role":"user","id":"A"}
  println(mapper.readValue<Identity>("""{"role":"admin","id":"B"}""")) // Admin(id=B)
}
```

##### Wrapped object

```kotlin
sealed class Result {
  companion object : Polymorphic {
    override val valueKey = "payload"
  }
}

data class Success<T>(val value: T) : Result()
data class Failure(val message: String) : Result()

fun main() {
  val mapper = jacksonObjectMapper().registerModule(PolymorphicModule())

  println(mapper.writeValueAsString(Success(30))) // {"type":"Success","payload":{"value":30}}
  println(mapper.readValue<Result>("""{"type":"Failure","payload":{"message":"Unknown"}}""")) // Failure(message=Unknown)
}
```

## Textual
Representation as JSON string by supplied conversion rules.

### Basic usage

```kotlin
data class Hexadecimal(val value: Int) {
  companion object : Textual<Hexadecimal> {
    override val Hexadecimal.text
      get() = value.toString(16)

    override fun fromText(text: String) =
        Hexadecimal(text.toInt(16))
  }
}

fun main() {
  val mapper = ObjectMapper().registerModule(TextualModule())

  println(mapper.writeValueAsString(Hexadecimal(1000))) // "3e8"
  println(mapper.readValue<Hexadecimal>(""""2a"""")) // Hexadecimal(value=42)
}
```

### Installation

Gradle Kotlin DSL

```kotlin
repositories {
  maven("https://jitpack.io")
}
dependencies {
  implementation("io.github.justincase-jp.jackson-kotlin-commons", "textual", VERSION)
}
```

### Caveats

*Currently the type parameter of `Textual` is not checked against its companion type,
so please be careful if you want to support a selected subset of possible values.*
