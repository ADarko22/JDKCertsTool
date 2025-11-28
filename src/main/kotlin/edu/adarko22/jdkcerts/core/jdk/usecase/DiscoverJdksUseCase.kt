package edu.adarko22.jdkcerts.core.jdk.usecase

import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.infra.system.JdkPathsDiscovery
import edu.adarko22.jdkcerts.infra.system.KeystoreInfoResolver
import java.nio.file.Path

/**
 * Use case for discovering installed JDKs on the system and creating [Jdk] objects.
 *
 * It combines JDK path discovery, Java version resolution, and keystore information
 * to produce fully populated [Jdk] instances.
 *
 * @param jdkPathsDiscovery Component responsible for finding JDK directories.
 * @param keystoreInfoResolver Component responsible for resolving keystore paths and settings.
 * @param resolveJavaInfo Use case for resolving Java version and vendor information.
 */
class DiscoverJdksUseCase(
    private val jdkPathsDiscovery: JdkPathsDiscovery,
    private val keystoreInfoResolver: KeystoreInfoResolver,
    private val resolveJavaInfo: ResolveJavaInfoUseCase,
) {
    /**
     * Discovers all JDKs, optionally including custom directories.
     *
     * @param customJdkDirs Additional directories to search for JDKs.
     * @return List of discovered [Jdk] objects with resolved Java and keystore info.
     */
    fun discover(customJdkDirs: List<Path> = emptyList()): List<Jdk> {
        val jdkPaths = jdkPathsDiscovery.discover(customJdkDirs)
        return jdkPaths.map { jdkPath -> createJdk(jdkPath) }
    }

    /**
     * Creates a [Jdk] instance from a given path by resolving Java info and keystore info.
     */
    private fun createJdk(jdkPath: Path): Jdk {
        val javaInfo = resolveJavaInfo.resolve(jdkPath)
        val keystoreInfo = keystoreInfoResolver.resolve(jdkPath, javaInfo)
        return Jdk(
            path = jdkPath,
            javaInfo = javaInfo,
            keystoreInfo = keystoreInfo,
        )
    }
}
