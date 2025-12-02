package edu.adarko22.jdkcerts.core.jdk

/**
 * Represents Java version information for a specific JDK.
 *
 * @property vendor The JDK vendor (e.g., Oracle, OpenJDK, Temurin).
 * @property fullVersion Full version string (e.g., "17.0.7").
 * @property major Major version number (e.g., 17).
 */
data class JavaInfo(
    val vendor: String,
    val fullVersion: String,
    val major: Int,
)
