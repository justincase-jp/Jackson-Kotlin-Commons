plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlin("reflect"))
  api(platform(kotlin("bom")))

  implementation(project(":internal"))

  api(jacksonDataBinding())
}
