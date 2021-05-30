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
  api(platform(kotlin("bom")))

  implementation(project(":internal"))
  implementation(project(":internal-primitive-codec"))
  implementation(project(":numerical-interface"))

  api(jacksonDataBinding())
  implementation(guava())

  testImplementation(kotlinTestJUnit5Runner())
  testImplementation(jacksonKotlinModule())
}
