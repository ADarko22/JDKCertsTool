package edu.adarko22.jdkcerts.jdk.impl

class JavaInfoParser {

    fun parseVersionInfo(javaVersionOutput: String): JavaInfo {
        val lines = javaVersionOutput.lines()
        val versionString =
            lines
                .firstOrNull { "version" in it }
                ?.let { Regex("\"([^\"]+)\"").find(it)?.groupValues?.get(1) }
                ?: throw IllegalStateException("No version string found")

        val major =
            when {
                versionString.startsWith("1.") -> versionString.substring(2, 3).toInt()

                else -> versionString.split(".")[0].toInt()
            }

        return JavaInfo(
            vendor = detectVendor(lines),
            fullVersion = versionString,
            major = major,
        )
    }

    private fun detectVendor(lines: List<String>): String {
        val output = lines.joinToString("\n")

        return when {
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

            // == Default Fallback ==
            else -> "OpenJDK"
        }
    }
}
