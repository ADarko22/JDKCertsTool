pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

//  Gradle's Foojay toolchain resolver convention plugin
//  to locate and download JDK distributions for Java toolchains (i.e. jvmToolchain)
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "JDKCertsTool"
