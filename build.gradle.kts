plugins {
    application
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
    alias(libs.plugins.sonar)
}

// Project metadata
group = findProperty("projectGroup") as String
version = findProperty("projectVersion") as String

// Repositories
repositories {
    mavenCentral()
    gradlePluginPortal()
}

// Dependencies
dependencies {
    // cli
    implementation(libs.clikt)

    // clid + core + infra
    implementation(libs.coroutines)

    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.test)

    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.reporting)
}

dependencyLocking {
    lockAllConfigurations()
}

// Toolchain
kotlin {
    jvmToolchain(21)
}

// Application configuration
application {
    mainClass.set(findProperty("applicationMainClass") as String)
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

// Sonar configration
sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "adarko22-dev")
        property("sonar.projectKey", "ADarko22_JDKCertsTool")

        property(
            "sonar.coverage.exclusions",
            "**/cli/**/*",
        )
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
