import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.10" apply false
}

tasks.getByName<Wrapper>("wrapper") {
  gradleVersion = "6.8.3"
}

subprojects {
  repositories {
    mavenCentral()
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
  }
}
