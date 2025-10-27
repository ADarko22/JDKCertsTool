package edu.adarko22.jdkcerts.jdk.impl.keystore

import edu.adarko22.jdkcerts.jdk.impl.JavaInfo
import java.nio.file.Files
import java.nio.file.Path

object UNIXKeystoreInfoResolver : KeystoreInfoResolver {
    override fun resolve(
        jdkPath: Path,
        javaInfo: JavaInfo,
    ): KeystoreInfo = KeystoreInfo(findCacertsFolder(jdkPath), javaInfo.major > 8)

    private fun findCacertsFolder(jdkPath: Path): Path =
        listOf(
            jdkPath.resolve("lib/security/cacerts"),
            jdkPath.resolve("jre/lib/security/cacerts"),
        ).firstOrNull { Files.exists(it) }
            .let { it ?: throw IllegalStateException("Could not find cacerts file in JDK at $jdkPath") }
}
