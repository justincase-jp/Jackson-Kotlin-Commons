plugins {
  maven
  `java-library`
}

dependencies {
  api(project(":textual-codec"))
  api(project(":textual-interface"))

  api(jacksonKotlinModule())
}
