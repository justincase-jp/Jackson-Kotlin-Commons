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

  implementation(project(":core"))

  api(jacksonDataBinding())
  implementation(guava())

  testImplementation(kotlinTestJUnit5Runner())
}
