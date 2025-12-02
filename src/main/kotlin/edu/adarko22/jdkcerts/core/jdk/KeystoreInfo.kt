package edu.adarko22.jdkcerts.core.jdk

import java.nio.file.Path

/**
 * Represents information about a JDK keystore.
 *
 * @property keystorePath Path to the keystore file.
 * @property cacertsShortcutEnabled True if the default `-cacerts` shortcut can be used.
 */
data class KeystoreInfo(
    val keystorePath: Path,
    val cacertsShortcutEnabled: Boolean,
)
