plugins {
  maven
  `java-library`
}

dependencies {
  api(project(":numerical-codec"))
  api(project(":numerical-interface"))

  api(jacksonKotlinModule())
}
