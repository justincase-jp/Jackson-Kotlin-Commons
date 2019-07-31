plugins {
  maven
  `java-library`
}

dependencies {
  api(project(":polymorphic-codec"))
  api(project(":polymorphic-interface"))

  api(jacksonKotlinModule())
}
