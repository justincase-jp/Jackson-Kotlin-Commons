plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlinLibrary("reflect"))
  api(platform(kotlin("bom")))

  implementation(project(":internal"))

  api(jacksonDataBinding())
  api(platform(jacksonBom()))
}
