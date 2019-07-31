plugins {
  maven
  `java-library`
}

dependencies {
  api(project(":numeric-codec"))
  api(project(":numeric-interface"))

  api(jacksonKotlinModule())
}
