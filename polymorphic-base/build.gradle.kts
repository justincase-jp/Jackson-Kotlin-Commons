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

  api("com.fasterxml.jackson.core", "jackson-databind", jackson_version)
  implementation("com.google.guava", "guava", "28.0-jre")

  testImplementation("io.kotlintest", "kotlintest-runner-junit5", "3.4.0")
  testImplementation(jacksonKotlinModule())
}
