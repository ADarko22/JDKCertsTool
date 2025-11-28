package edu.adarko22.jdkcerts.infra.system

import edu.adarko22.jdkcerts.infra.system.unix.UNIXJdkPathsDiscovery
import edu.adarko22.jdkcerts.infra.system.unix.UNIXKeystoreInfoResolver
import edu.adarko22.jdkcerts.infra.system.unix.UNIXSystemInfoProvider

/**
 * Represents the host system type and provides system-specific implementations
 * for discovering JDK paths and resolving keystore information.
 */
enum class SystemType {
    /**
     * Unix or macOS systems.
     */
    UNIX {
        private val unixSystemInfoProvider = UNIXSystemInfoProvider()

        override fun jdkPathDiscovery() = UNIXJdkPathsDiscovery(unixSystemInfoProvider)

        override fun keystoreInfoResolver() = UNIXKeystoreInfoResolver()
    }, ;

    /**
     * Returns a system-specific [JdkPathsDiscovery] implementation.
     */
    abstract fun jdkPathDiscovery(): JdkPathsDiscovery

    /**
     * Returns a system-specific [KeystoreInfoResolver] implementation.
     */
    abstract fun keystoreInfoResolver(): KeystoreInfoResolver
}
