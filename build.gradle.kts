plugins {
    kotlin("jvm") version "1.3.61"
    `maven-publish`
    id("org.jetbrains.dokka") version "0.10.1"
}

group = "bakiev.artour"
version = "0.1"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("junit:junit:4.13")
    testImplementation("org.amshove.kluent:kluent:1.37")
}

tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
