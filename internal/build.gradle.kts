plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlinLibrary("reflect"))
  api(platform(kotlin("bom")))

  implementation(jacksonDataBinding())
  api(platform(jacksonBom()))
  implementation(guava())
}
