plugins {
    kotlin("jvm") version "1.3.61"
}

group = "bakiev.artour"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("junit:junit:4.13")
    testImplementation("org.amshove.kluent:kluent:1.37")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}