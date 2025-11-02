package edu.adarko22.jdkcerts.system

import edu.adarko22.jdkcerts.system.unix.UNIXJdkPathsDiscovery
import edu.adarko22.jdkcerts.system.unix.UNIXKeystoreInfoResolver
import edu.adarko22.jdkcerts.system.unix.UNIXSystemInfoProvider

enum class SystemType {
    UNIX {
        private val unixSystemInfoProvider = UNIXSystemInfoProvider()

        override fun jdkPathDiscovery() = UNIXJdkPathsDiscovery(unixSystemInfoProvider)

        override fun keystoreInfoResolver() = UNIXKeystoreInfoResolver()
    }, ;

    abstract fun jdkPathDiscovery(): JdkPathsDiscovery

    abstract fun keystoreInfoResolver(): KeystoreInfoResolver
}
