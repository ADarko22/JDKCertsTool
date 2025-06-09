plugins {
    application
    kotlin("jvm") version "2.1.10"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0-rc.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    jacoco
}

group = "edu.adarko22"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:5.0.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.0-M1")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0-M1")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("edu.adarko22.MainKt")
}

ktlint {
    android.set(false)
    outputColorName.set("RED")
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

tasks.test {
    useJUnitPlatform()
    reports {
        junitXml.required.set(true)
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("JDKCertsTool")
    archiveClassifier.set("")
    archiveVersion.set(version.toString())
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        attributes["Implementation-Version"] = archiveVersion.get()
    }
}

// Fix implicit dependency errors by wiring dependent tasks explicitly
tasks.named<Zip>("distZip") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<Tar>("distTar") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<CreateStartScripts>("startScripts") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named("shadowJar"))
    classpath =
        files(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get().archiveFile)
}
