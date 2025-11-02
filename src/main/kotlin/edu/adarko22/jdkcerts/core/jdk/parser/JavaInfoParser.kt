package edu.adarko22.jdkcerts.core.jdk.parser

import edu.adarko22.jdkcerts.core.jdk.JavaInfo

interface JavaInfoParser {
    fun parseVersionInfo(javaVersionOutput: String): JavaInfo
}
