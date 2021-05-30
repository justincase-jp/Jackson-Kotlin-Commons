plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlinLibrary("reflect"))
  api(platform(kotlin("bom")))
}
