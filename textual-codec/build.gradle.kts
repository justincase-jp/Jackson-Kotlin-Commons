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
  implementation(kotlinLibrary("reflect"))
  api(platform(kotlin("bom")))

  implementation(project(":internal"))
  implementation(project(":internal-primitive-codec"))
  implementation(project(":textual-interface"))
  implementation(project(":enumerated"))

  api(jacksonDataBinding())
  api(platform(jacksonBom()))
  implementation(guava())

  testImplementation(kotlinTestJUnit5Runner())
  testImplementation(jacksonKotlinModule())
}
