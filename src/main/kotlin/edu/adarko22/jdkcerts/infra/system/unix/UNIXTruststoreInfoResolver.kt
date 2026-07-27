package edu.adarko22.jdkcerts.infra.system.unix

import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.TruststoreInfo
import edu.adarko22.jdkcerts.infra.system.TruststoreInfoResolver
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unix/macOS implementation of [TruststoreInfoResolver].
 *
 * Determines the path to the JDK's `cacerts` file and whether the
 * `-cacerts` shortcut is supported (Java 9+).
 */
class UNIXTruststoreInfoResolver : TruststoreInfoResolver {
    override fun resolve(
        jdkPath: Path,
        javaInfo: JavaInfo,
    ): TruststoreInfo = TruststoreInfo(findCacertsFolder(jdkPath), javaInfo.major > 8)

    private fun findCacertsFolder(jdkPath: Path): Path =
        listOf(
            jdkPath.resolve("lib/security/cacerts"),
            jdkPath.resolve("jre/lib/security/cacerts"),
        ).firstOrNull { Files.exists(it) }
            .let { it ?: throw IllegalStateException("Could not find cacerts file in JDK at $jdkPath") }
}
