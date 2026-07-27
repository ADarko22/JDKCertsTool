package edu.adarko22.jdkcerts.infra.system

import edu.adarko22.jdkcerts.infra.system.unix.UNIXJdkPathsDiscovery
import edu.adarko22.jdkcerts.infra.system.unix.UNIXSystemInfoProvider
import edu.adarko22.jdkcerts.infra.system.unix.UNIXTruststoreInfoResolver

/**
 * Represents the host system type and provides system-specific implementations
 * for discovering JDK paths and resolving truststore information.
 */
enum class SystemType {
    /**
     * Unix or macOS systems.
     */
    UNIX {
        private val unixSystemInfoProvider = UNIXSystemInfoProvider()

        override fun jdkPathDiscovery() = UNIXJdkPathsDiscovery(unixSystemInfoProvider)

        override fun truststoreInfoResolver() = UNIXTruststoreInfoResolver()
    }, ;

    /**
     * Returns a system-specific [JdkPathsDiscovery] implementation.
     */
    abstract fun jdkPathDiscovery(): JdkPathsDiscovery

    /**
     * Returns a system-specific [TruststoreInfoResolver] implementation.
     */
    abstract fun truststoreInfoResolver(): TruststoreInfoResolver
}
