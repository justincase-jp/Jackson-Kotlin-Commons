plugins {
  `java-library`
}

dependencies {
  api(project(":polymorphic-base"))
  api(jacksonKotlinModule())
}
