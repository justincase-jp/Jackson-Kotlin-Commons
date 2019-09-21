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
  implementation(kotlin("reflect"))

  implementation(project(":internal"))
  implementation(project(":internal-primitive-codec"))
  implementation(project(":textual-interface"))
  implementation(project(":enumerated"))

  api(jacksonDataBinding())
  implementation(guava())

  testImplementation(kotlinTestJUnit5Runner())
  testImplementation(jacksonKotlinModule())
}
