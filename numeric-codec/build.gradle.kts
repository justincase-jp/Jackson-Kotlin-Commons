plugins {
  maven
  kotlin("jvm")
  `java-library`
}

tasks.getByName("test", Test::class) {
  @Suppress("UnstableApiUsage")
  useJUnitPlatform()
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("reflect"))

  implementation(project(":internal"))
  implementation(project(":internal-primitive-codec"))
  implementation(project(":numeric-interface"))

  api(jacksonDataBinding())
  implementation(guava())

  testImplementation(kotlinTestJUnit5Runner())
  testImplementation(jacksonKotlinModule())
}
