package edu.adarko22.jdkcerts.core.jdk.parser

import edu.adarko22.jdkcerts.core.jdk.JavaInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class JavaInfoParserTest {
    @ParameterizedTest(name = "#{index}: {0}")
    @MethodSource("versionTestCases")
    @DisplayName("Should parse Java version information correctly")
    fun `test version parsing`(testCase: VersionTestCase) {
        val actualVersion = DefaultJavaInfoParser().parseVersionInfo(testCase.output)
        assertEquals(testCase.expectedVersion, actualVersion, "Version parsing failed for: ${testCase.description}")
    }

    companion object {
        @JvmStatic
        fun versionTestCases(): Stream<VersionTestCase> =
            Stream.of(
                VersionTestCase.Oracle22,
                VersionTestCase.Temurin21,
                VersionTestCase.OpenJDK21,
                VersionTestCase.Temurin17,
                VersionTestCase.GraalVM25,
                VersionTestCase.Zulu8,
                VersionTestCase.IbmSemeru11,
                VersionTestCase.OpenJDK25,
                VersionTestCase.Jetbrains21,
            )
    }
}

sealed class VersionTestCase(
    val output: String,
    val expectedVersion: JavaInfo,
    val description: String,
) {
    override fun toString() = description

    object Oracle22 : VersionTestCase(
        output =
            """
            java version "22.0.2" 2024-07-16
            Java(TM) SE Runtime Environment (build 22.0.2+9-70)
            Java HotSpot(TM) 64-Bit Server VM (build 22.0.2+9-70, mixed mode, sharing)
            """.trimIndent(),
        expectedVersion = JavaInfo("Oracle", "22.0.2", 22),
        description = "Oracle Java 22",
    )

    object Temurin21 : VersionTestCase(
        output =
            """
            openjdk version "21.0.5" 2024-10-15 LTS
            OpenJDK Runtime Environment Temurin-21.0.5+11 (build 21.0.5+11-LTS)
            OpenJDK 64-Bit Server VM Temurin-21.0.5+11 (build 21.0.5+11-LTS, mixed mode, sharing)
            """.trimIndent(),
        expectedVersion = JavaInfo("Temurin", "21.0.5", 21),
        description = "Temurin OpenJDK 21",
    )

    object OpenJDK21 : VersionTestCase(
        output =
            """
            openjdk version "21.0.6" 2025-01-21 LTS
            OpenJDK Runtime Environment (build 21.0.6+10-LTS)
            OpenJDK 64-Bit Server VM (build 21.0.6+10-LTS, mixed mode, sharing)
            """.trimIndent(),
        expectedVersion = JavaInfo("OpenJDK", "21.0.6", 21),
        description = "OpenJDK 21",
    )

    object Temurin17 : VersionTestCase(
        output =
            """
            openjdk version "17.0.13" 2024-10-15
            OpenJDK Runtime Environment Temurin-17.0.13+11 (build 17.0.13+11)
            OpenJDK 64-Bit Server VM Temurin-17.0.13+11 (build 17.0.13+11, mixed mode, sharing)
            """.trimIndent(),
        expectedVersion = JavaInfo("Temurin", "17.0.13", 17),
        description = "Temurin OpenJDK 17",
    )

    object GraalVM25 : VersionTestCase(
        output =
            """
            openjdk version "25.0.1" 2025-10-21
            OpenJDK Runtime Environment GraalVM CE 25.0.1+8.1 (build 25.0.1+8-jvmci-b01)
            OpenJDK 64-Bit Server VM GraalVM CE 25.0.1+8.1 (build 25.0.1+8-jvmci-b01, mixed mode, sharing)
            """.trimIndent(),
        expectedVersion = JavaInfo("GraalVM", "25.0.1", 25),
        description = "GraalVM 25",
    )

    object Zulu8 : VersionTestCase(
        output =
            """
            openjdk version "1.8.0_472"
            OpenJDK Runtime Environment (Zulu 8.90.0.19-CA-macos-aarch64) (build 1.8.0_472-b08)
            OpenJDK 64-Bit Server VM (Zulu 8.90.0.19-CA-macos-aarch64) (build 25.472-b08, mixed mode)
            """.trimIndent(),
        expectedVersion = JavaInfo("Zulu", "1.8.0_472", 8),
        description = "OpenJDK 8 (Zulu)",
    )

    object IbmSemeru11 : VersionTestCase(
        output =
            """
            openjdk version "11.0.28" 2025-07-15
            IBM Semeru Runtime Open Edition 11.0.28.0 (build 11.0.28+6)
            Eclipse OpenJ9 VM 11.0.28.0 (build openj9-0.53.0, JRE 11 Mac OS X aarch64-64-Bit 20250722_976 (JIT enabled, AOT enabled)
            OpenJ9   - 017819f167
            OMR      - 266a8c6f5
            JCL      - 36f5ad5848 based on jdk-11.0.28+6)
            """.trimIndent(),
        expectedVersion = JavaInfo("Semeru", "11.0.28", 11),
        description = "IBM Semeru with OpenJ9",
    )

    object OpenJDK25 : VersionTestCase(
        output =
            """
            openjdk version "25.0.1" 2025-10-21
            OpenJDK Runtime Environment (build 25.0.1+8-27)
            OpenJDK 64-Bit Server VM (build 25.0.1+8-27, mixed mode, sharing)
            """.trimIndent(),
        expectedVersion = JavaInfo("OpenJDK", "25.0.1", 25),
        description = "OpenJDK 25",
    )

    object Jetbrains21 : VersionTestCase(
        output =
            """
            openjdk version "21.0.8" 2025-07-15
            OpenJDK Runtime Environment JBR-21.0.8+9-1038.68-jcef (build 21.0.8+9-b1038.68)
            OpenJDK 64-Bit Server VM JBR-21.0.8+9-1038.68-jcef (build 21.0.8+9-b1038.68, mixed mode, sharing)
            """.trimIndent(),
        expectedVersion = JavaInfo("JetBrains", "21.0.8", 21),
        description = "JetBrains Runtime",
    )
}
