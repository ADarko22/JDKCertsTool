plugins {
    application
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
}

// Project metadata
group = "edu.adarko22.jdkcerts"
version = "1.0.0"

// Repositories
repositories {
    mavenCentral()
}

// Dependencies
dependencies {
    implementation(libs.clikt)

    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.reporting)
}

// Toolchain
kotlin {
    jvmToolchain(21)
}

// Application configuration
application {
    mainClass.set("edu.adarko22.jdkcerts.MainKt")
}

// Ktlint configuration
ktlint {
    android.set(false)
    outputColorName.set("RED")
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

// Test configuration
tasks.test {
    useJUnitPlatform()
    reports {
        junitXml.required.set(true)
    }
    finalizedBy(tasks.jacocoTestReport)
}

// Jacoco report
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Packaging
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

// Distribution tasks
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
    classpath = files(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get().archiveFile)
}
