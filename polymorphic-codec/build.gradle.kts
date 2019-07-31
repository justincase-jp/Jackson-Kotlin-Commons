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
  implementation(project(":polymorphic-interface"))

  api(jacksonDataBinding())
  implementation("com.google.guava", "guava", "28.0-jre")

  testImplementation(kotlinTestJUnit5Runner())
  testImplementation(jacksonKotlinModule())
}
