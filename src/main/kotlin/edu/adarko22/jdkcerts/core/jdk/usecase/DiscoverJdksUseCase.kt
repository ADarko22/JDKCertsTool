package edu.adarko22.jdkcerts.core.jdk.usecase

import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.infra.system.JdkPathsDiscovery
import edu.adarko22.jdkcerts.infra.system.KeystoreInfoResolver
import java.nio.file.Path

class DiscoverJdksUseCase(
    private val jdkPathsDiscovery: JdkPathsDiscovery,
    private val keystoreInfoResolver: KeystoreInfoResolver,
    private val resolveJavaInfo: ResolveJavaInfoUseCase,
) {
    fun discover(customJdkDirs: List<Path> = emptyList()): List<Jdk> {
        val jdkPaths = jdkPathsDiscovery.discover(customJdkDirs)
        return jdkPaths.map { jdkPath -> createJdk(jdkPath) }
    }

    fun createJdk(jdkPath: Path): Jdk {
        val javaInfo = resolveJavaInfo.resolve(jdkPath)
        val keystoreInfo = keystoreInfoResolver.resolve(jdkPath, javaInfo)
        return Jdk(
            path = jdkPath,
            javaInfo = javaInfo,
            keystoreInfo = keystoreInfo,
        )
    }
}
