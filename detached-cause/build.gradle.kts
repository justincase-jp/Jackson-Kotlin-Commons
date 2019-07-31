plugins {
  maven
  kotlin("jvm")
  `java-library`
}

dependencies {
  implementation(kotlin("stdlib"))

  implementation(project(":internal"))
}
