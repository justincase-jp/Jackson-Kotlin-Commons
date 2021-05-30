plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlinLibrary("stdlib"))
  api(platform(kotlin("bom")))

  implementation(project(":internal"))

  implementation(jacksonDataBinding())
}
