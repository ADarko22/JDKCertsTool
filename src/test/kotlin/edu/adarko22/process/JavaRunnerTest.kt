package edu.adarko22.process

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.file.Path

class JavaRunnerTest {
    @TempDir
    lateinit var tempDir: Path

    @ParameterizedTest(name = "Parse JDK version from output: {1}")
    @CsvSource(
        // versionOutput, expectedVersion (null for unknown)
        // Java 8
        "'java version \"1.8.0_202\"" +
            "\nJava(TM) SE Runtime Environment (build 1.8.0_202-b08)" +
            "\nJava HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)', 8",
        // Java 11
        "'openjdk version \"11.0.2\" 2019-01-15" +
            "\nOpenJDK Runtime Environment AdoptOpenJDK (build 11.0.2+9)" +
            "\nOpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.2+9, mixed mode)', 11",
        // No Java version
        "'some random output without version info', ''",
    )
    fun `getMajorVersion parses various versions`(
        versionOutput: String,
        expectedVersion: String?,
    ) {
        val executor = mock<ProcessExecutor>()
        val javaPath = tempDir.resolve("bin/java").toAbsolutePath().toString()
        val command = listOf(javaPath, "-version")

        // JUnit CsvSource can't represent empty string as null, so treat empty string as null
        val expected = expectedVersion?.takeIf { it.isNotBlank() }?.toIntOrNull()

        whenever(executor.runCommand(command)).thenReturn(
            ProcessResult(stdout = "", stderr = versionOutput, exitCode = 0),
        )

        val javaRunner = JavaRunner(executor)
        val version = javaRunner.getMajorVersion(tempDir)
        assertEquals(expected, version)
    }
}
