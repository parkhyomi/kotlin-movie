plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

group = "camp.nextstep.edu"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.14.3")
    testImplementation("org.assertj", "assertj-core", "3.27.7")
    testImplementation("io.kotest", "kotest-runner-junit5", "6.0.7")

    runtimeOnly("com.h2database:h2:2.4.240")
}

tasks {
    test {
        useJUnitPlatform()
    }
    ktlint {
        verbose.set(true)
    }
}
