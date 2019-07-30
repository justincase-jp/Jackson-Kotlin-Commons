plugins {
  maven
  `java-library`
}

dependencies {
  api(project(":polymorphic-codec"))
  api(jacksonKotlinModule())
}
