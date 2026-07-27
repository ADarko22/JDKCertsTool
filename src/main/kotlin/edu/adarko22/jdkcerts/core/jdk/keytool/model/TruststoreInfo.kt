package edu.adarko22.jdkcerts.core.jdk.keytool.model

import java.nio.file.Path

/**
 * Represents information about a JDK truststore.
 *
 * @property truststorePath Path to the truststore file.
 * @property cacertsShortcutEnabled True if the default `-cacerts` shortcut can be used.
 */
data class TruststoreInfo(
    val truststorePath: Path,
    val cacertsShortcutEnabled: Boolean,
)
