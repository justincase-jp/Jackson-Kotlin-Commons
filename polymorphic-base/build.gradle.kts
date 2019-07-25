plugins {
  kotlin("jvm")
  `java-library`
}

dependencies {
  api(kotlin("stdlib"))
  api("com.fasterxml.jackson.core", "jackson-databind", jackson_version)

  testImplementation("io.kotlintest", "kotlintest-runner-junit5", "3.4.0")
  testImplementation(jacksonKotlinModule())
}
