plugins {
    application
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
    alias(libs.plugins.sonar)
    alias(libs.plugins.graalvm.native)
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
    testImplementation(libs.archunit)
    testImplementation(libs.archunit.junit6)

    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.reporting)
}

dependencyLocking {
    lockAllConfigurations()
}

// Toolchain
kotlin {
    jvmToolchain(25)
}

// Application configuration
application {
    mainClass.set(findProperty("applicationMainClass") as String)
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        attributes["Implementation-Version"] = project.version.toString()
    }
}

// `run` uses the assembled jar (rather than the default raw classpath) so that
// InfoCliCommand's Package.getImplementationVersion() resolves correctly locally too.
tasks.named<JavaExec>("run") {
    dependsOn(tasks.named("jar"))
    classpath = files(tasks.named<Jar>("jar").get().archiveFile) + configurations.runtimeClasspath.get()
}

// Fat jar published alongside the native binaries as a manual-install alternative for
// users who already have a Java 25+ runtime. Not wired into distZip/distTar/startScripts/run.
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("JDKCertsTool")
    archiveClassifier.set("all") // avoids colliding with the plain `jar` task's output path
    archiveVersion.set(version.toString())
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        attributes["Implementation-Version"] = archiveVersion.get()
    }
}

// GraalVM native-image configuration
graalvmNative {
    binaries {
        named("main") {
            imageName.set("jdkcerts")
            mainClass.set(findProperty("applicationMainClass") as String)
            buildArgs.addAll(
                "-march=compatibility",
            )
        }
    }
    // Mordant (pulled in transitively by Clikt) uses JNA for terminal capability
    // detection on macOS (com.github.ajalt.mordant.terminal.terminalinterface.jna.*),
    // confirmed via the native-image tracing agent. JNA's reflect/JNI config comes
    // from this community-maintained repository, not from this project.
    metadataRepository {
        enabled.set(true)
        version.set("1.0.13")
    }
}

// Ktlint configuration
ktlint {
    // Force engine to match the version used by Super-Linter (CI)
    version.set("1.8.0")
    android.set(false)
    ignoreFailures.set(false)
    outputColorName.set("RED")

    filter {
        // Explicitly include the kotlin source files
        include("**/*.kt")
    }

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
