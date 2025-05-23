plugins {
    application
    kotlin("jvm") version "2.1.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "edu.adarko22"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.5.2")

    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("edu.adarko22.MainKt")
}
tasks {
    shadowJar {
        archiveBaseName.set("JDKCertsTool")
        archiveClassifier.set("")
        archiveVersion.set(version.toString())
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
    }
}
