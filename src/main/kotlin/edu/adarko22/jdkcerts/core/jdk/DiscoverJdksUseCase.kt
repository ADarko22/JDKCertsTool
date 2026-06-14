package edu.adarko22.jdkcerts.core.jdk

import edu.adarko22.jdkcerts.core.jdk.java.usecase.ResolveJavaInfoUseCase
import edu.adarko22.jdkcerts.infra.system.JdkPathsDiscovery
import edu.adarko22.jdkcerts.infra.system.KeystoreInfoResolver
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.nio.file.Path

/**
 * Orchestrates the discovery and parallel assembly of [Jdk] domain objects.
 *
 * This use case acts as a high-performance factory pipeline. It first scans the file system
 * for valid JDK installation paths, and then concurrently resolves the heavy I/O metadata
 * (Java version, vendor details, and keystore configurations) for every discovered path.
 *
 * **Architectural Notes:**
 * - **I/O Parallelism:** By delegating the metadata resolution to concurrent `async` blocks,
 * slow operations (like spawning `java -version` processes) on one JDK will not block the processing of others.
 * - **Structured Concurrency:** This pipeline runs within a `coroutineScope`. This provides a strict guarantee:
 * if the discovery process is canceled by the user (e.g., CLI exit), all underlying active I/O tasks are immediately terminated.
 *
 * @param jdkPathsDiscovery Component responsible for sweeping the file system for JDK root directories.
 * @param keystoreInfoResolver Component responsible for determining the default keystore path and password.
 * @param resolveJavaInfo Use case that extracts Java vendor and version by invoking the JDK's binary.
 */
class DiscoverJdksUseCase(
    private val jdkPathsDiscovery: JdkPathsDiscovery,
    private val keystoreInfoResolver: KeystoreInfoResolver,
    private val resolveJavaInfo: ResolveJavaInfoUseCase,
) {
    /**
     * Sweeps the system for JDKs and resolves their metadata concurrently.
     *
     * **Error Handling:** Because this operates under a `coroutineScope`, an unhandled exception
     * thrown during the resolution of a single JDK will cancel the `async` jobs of all other JDKs
     * and bubble up to the caller.
     *
     * @param customJdkDirs Optional user-provided directories to search in addition to system defaults.
     * @return A list of fully populated [Jdk] instances, ready for domain operations.
     */
    suspend fun discover(customJdkDirs: List<Path> = emptyList()): List<Jdk> =
        coroutineScope {
            val jdkPaths = jdkPathsDiscovery.discover(customJdkDirs)
            jdkPaths.map { jdkPath -> async { createJdk(jdkPath) } }.awaitAll()
        }

    /**
     * Internal factory method that orchestrates the data gathering for a single JDK path.
     * This function suspends while waiting for the underlying I/O resolution tasks to finish.
     */
    private suspend fun createJdk(jdkPath: Path): Jdk {
        val javaInfo = resolveJavaInfo.resolve(jdkPath)
        val keystoreInfo = keystoreInfoResolver.resolve(jdkPath, javaInfo)
        return Jdk(
            path = jdkPath,
            javaInfo = javaInfo,
            keystoreInfo = keystoreInfo,
        )
    }
}
