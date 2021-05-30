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

  implementation(guava())

  testImplementation(kotlinTestJUnit5Runner())
}
