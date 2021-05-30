
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.kotlin

fun DependencyHandler.kotlinLibrary(module: String): Dependency =
    create(kotlin(module)).run {
      this as ModuleDependency
      exclude("org.jetbrains", "annotations")
    }

fun jacksonDataBinding() = "com.fasterxml.jackson.core:jackson-databind:$jackson_version"
fun jacksonKotlinModule() = "com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version"

fun guava() = "com.google.guava:guava:28.0-jre"

fun kotlinTestJUnit5Runner() = "io.kotest:kotest-runner-junit5-jvm:4.4.3"
