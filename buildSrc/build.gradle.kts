import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `embedded-kotlin`
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
  }
}

repositories {
  // Current Kotlin DSL need JCenter to resolve stdlib for buildSrc compilation
  jcenter()
}
