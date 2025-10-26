package edu.adarko22.jdkcerts.jdk

import edu.adarko22.jdkcerts.jdk.java.JavaRunner
import org.junit.jupiter.api.Assertions
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
            Assertions.assertNull(result)
        } else if (expected.contains("EXPECTED_PATH")) {
            Assertions.assertNotNull(result)
            Assertions.assertEquals(2, result!!.size)
            Assertions.assertEquals("-keystore", result[0])
            Assertions.assertTrue(result[1].endsWith("cacerts"))
        } else {
            Assertions.assertEquals(expected, result)
        }
    }
}
