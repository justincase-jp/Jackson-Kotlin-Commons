plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlin("reflect"))

  implementation(project(":internal"))

  api(jacksonDataBinding())
}
