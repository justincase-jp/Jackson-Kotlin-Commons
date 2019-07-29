plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("reflect"))

  implementation(jacksonDataBinding())
  implementation(guava())
}
