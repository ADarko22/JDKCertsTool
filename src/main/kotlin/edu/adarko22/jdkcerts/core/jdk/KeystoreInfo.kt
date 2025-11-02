package edu.adarko22.jdkcerts.core.jdk

import java.nio.file.Path

/**
 * Information about a JDK's keystore.
 *
 * @property keystorePath The path to the keystore file.
 * @property cacertsShortcutEnabled Whether the cacerts shortcut, i.e. "-cacerts", is enabled for this JDK.
 */
data class KeystoreInfo(
    val keystorePath: Path,
    val cacertsShortcutEnabled: Boolean,
)
