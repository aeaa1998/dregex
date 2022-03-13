import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    application
}

group = "me.augustoalonso"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    val jgraphtVersion = "1.5.1"
    this.implementation("org.jgrapht:jgrapht-core:$jgraphtVersion")
    this.implementation("org.jgrapht:jgrapht-ext:$jgraphtVersion")
    implementation("de.vandermeer:asciitable:0.3.2")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}