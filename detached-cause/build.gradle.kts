plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlin("stdlib"))
  api(platform(kotlin("bom")))

  implementation(project(":internal"))

  implementation(jacksonDataBinding())
}
