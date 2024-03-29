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
  implementation(project(":polymorphic-interface"))

  api(jacksonDataBinding())
  api(platform(jacksonBom()))
  implementation("com.google.guava", "guava", "28.0-jre")

  testImplementation(kotlinTestJUnit5Runner())
  testImplementation(jacksonKotlinModule())
}
