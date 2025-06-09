package edu.adarko22.process

import edu.adarko22.runner.JavaRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

class KeystoreResolverTest {
    @TempDir
    lateinit var tempDir: Path

    companion object {
        @JvmStatic
        fun keystoreScenarios(): Stream<Arguments> =
            Stream.of(
                Arguments.of("JDK > 8", 11, true, listOf("-cacerts")),
                Arguments.of("JDK 8 with cacerts", 8, true, listOf("-keystore", "EXPECTED_PATH")),
                Arguments.of("JDK 8 without cacerts", 8, false, null),
                Arguments.of("Unknown JDK version", null, false, null),
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("keystoreScenarios")
    fun `test keystore resolution`(
        name: String,
        version: Int?,
        shouldCreateCacerts: Boolean,
        expected: List<String>?,
    ) {
        val jdkPath = tempDir.resolve(name.replace(" ", "_"))
        Files.createDirectories(jdkPath)

        // Create mock JavaRunner
        val javaRunner =
            mock<JavaRunner> {
                on { getMajorVersion(jdkPath) } doReturn version
            }

        // Optionally create a fake cacerts file
        if (shouldCreateCacerts) {
            val cacertsPath = jdkPath.resolve("lib/security/cacerts")
            Files.createDirectories(cacertsPath.parent)
            Files.createFile(cacertsPath)
        }

        val resolver = KeystoreResolver(javaRunner)
        val result = resolver.resolve(jdkPath)

        if (expected == null) {
            assertNull(result)
        } else if (expected.contains("EXPECTED_PATH")) {
            assertNotNull(result)
            assertEquals(2, result!!.size)
            assertEquals("-keystore", result[0])
            assertTrue(result[1].endsWith("cacerts"))
        } else {
            assertEquals(expected, result)
        }
    }
}
