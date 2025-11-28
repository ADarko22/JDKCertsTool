package edu.adarko22.jdkcerts.core.jdk.parser

import edu.adarko22.jdkcerts.core.jdk.JavaInfo

/**
 * Parses Java version output and extracts structured [JavaInfo].
 *
 * Implementations are responsible for converting raw `java -version` command
 * output into a [JavaInfo] object.
 */
interface JavaInfoParser {
    /**
     * Parses the provided Java version output.
     *
     * @param javaVersionOutput The raw output of `java -version`.
     * @return [JavaInfo] containing the extracted version information.
     */
    fun parseVersionInfo(javaVersionOutput: String): JavaInfo
}
