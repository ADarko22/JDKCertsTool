package edu.adarko22.jdkcerts.core.jdk.java.parser

import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo

/**
 * Default implementation of [JavaInfoParser] that parses typical Java version output.
 */
class DefaultJavaInfoParser : JavaInfoParser {
    companion object {
        // Compile regex once per classloader, not once per method call
        private val VERSION_REGEX = Regex("\"([^\"]+)\"")
    }

    override fun parseVersionInfo(javaVersionOutput: String): JavaInfo {
        val lines = javaVersionOutput.lines()

        // Safe regex matching
        val versionString =
            lines
                .firstOrNull { "version" in it }
                ?.let { VERSION_REGEX.find(it)?.groupValues?.get(1) }
                ?: throw IllegalStateException("No version string found in output '$javaVersionOutput'")

        // Safe numeric parsing without index bounds
        val majorStr =
            if (versionString.startsWith("1.")) {
                versionString.substringAfter("1.").substringBefore(".")
            } else {
                versionString.substringBefore(".")
            }

        val major = majorStr.toIntOrNull() ?: throw IllegalStateException("Could not parse major version from '$versionString'")

        return JavaInfo(
            vendor = detectVendor(javaVersionOutput),
            fullVersion = versionString,
            major = major,
        )
    }

    private fun detectVendor(output: String): String =
        when {
            // == Proprietary ==
            "Java(TM)" in output -> "Oracle"
            "GraalVM" in output -> "GraalVM"

            // == Specific OpenJDK Distributor Brands ==
            "Temurin" in output -> "Temurin"
            "Corretto" in output -> "Corretto"
            "Zulu" in output -> "Zulu"
            "JBR" in output -> "JetBrains" // JetBrains Runtime
            "Semeru" in output -> "Semeru"

            // == Other major OpenJDK distributors ==
            "Microsoft" in output -> "Microsoft"
            "Red Hat" in output -> "Red Hat"
            "SAP" in output -> "SAP"
            "BellSoft" in output || "Liberica" in output -> "BellSoft"

            // == Legacy or VM-based fallbacks ==
            "AdoptOpenJDK" in output -> "AdoptOpenJDK"
            "OpenJ9" in output -> "IBM"

            // Open JDK
            "openjdk" in output -> "OpenJDK"

            // == Default Fallback ==
            else -> "Unknown"
        }
}
