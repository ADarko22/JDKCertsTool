package edu.adarko22.commands

import com.github.ajalt.clikt.core.parse
import edu.adarko22.process.KeytoolRunner
import edu.adarko22.utils.JdkDiscovery
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class InstallCertsJdkCommandTest {

    private lateinit var mockPrinter: (String) -> Unit
    private lateinit var capturedOutput: MutableList<String>
    private lateinit var mockDiscovery: JdkDiscovery
    private lateinit var mockRunner: KeytoolRunner

    @TempDir
    lateinit var tempDir: Path

    private lateinit var certPath: Path
    private lateinit var jdkPath: Path

    @BeforeEach
    fun setup() {
        capturedOutput = mutableListOf()
        mockPrinter = { msg -> capturedOutput.add(msg) }

        mockDiscovery = mockk()
        mockRunner = mockk(relaxed = true)

        certPath = Files.createFile(tempDir.resolve("test-cert.pem"))
        jdkPath = tempDir.resolve("jdk").apply { Files.createDirectories(this) }
    }

    @Test
    fun `should install cert if file exists`() {
        every { mockDiscovery.discoverJdkHomes(any()) } returns listOf(jdkPath)
        every {
            mockRunner.runCommandWithCacertsResolution(
                any(),
                any(),
                any<List<String>>(),
                any()
            )
        } just Runs

        val cmd = InstallCertsJdkCommand(mockDiscovery, printer = mockPrinter, keytoolRunner = mockRunner)

        val args = arrayOf("--cert", certPath.toString(), "--alias", "my-cert")
        cmd.parse(args)
        cmd.run()

        verify {
            mockRunner.runCommandWithCacertsResolution(
                match { it.contains("certificate installation") },
                listOf(jdkPath),
                match { it.containsAll(listOf("-importcert", "-alias", "my-cert")) },
                false
            )
        }

        assertTrue(capturedOutput.any { it.contains("Found JDKs") }, "Expected to log JDK discovery")
    }

    @Test
    fun `should print error and skip if cert file missing`() {
        val fakeCertPath = tempDir.resolve("missing.pem")

        val cmd = InstallCertsJdkCommand(mockDiscovery, printer = mockPrinter, keytoolRunner = mockRunner)

        val args = arrayOf("--cert", fakeCertPath.toString(), "--alias", "my-cert")
        cmd.parse(args)
        cmd.run()

        verify(exactly = 0) { mockRunner.runCommandWithCacertsResolution(any(), any(), any(), any()) }

        assertTrue(
            capturedOutput.any { it.contains("❌ Certificate not found") },
            "Expected error about missing certificate"
        )
    }

    @Test
    fun `should continue on dry run even if cert file is missing`() {
        val fakeCertPath = tempDir.resolve("missing.pem")

        every { mockDiscovery.discoverJdkHomes(any()) } returns emptyList()

        val cmd = InstallCertsJdkCommand(mockDiscovery, printer = mockPrinter, keytoolRunner = mockRunner)

        val args = arrayOf("--cert", fakeCertPath.toString(), "--alias", "test", "--dry-run")
        cmd.parse(args)
        cmd.run()

        assertAll(
            "dry run with missing cert",
            {
                assertTrue(
                    capturedOutput.any { it.contains("❌ Certificate not found") },
                    "Expected error about missing certificate"
                )
            },
            {
                assertTrue(capturedOutput.any { it.contains("Dry run") }, "Expected dry run continuation message")
            }
        )
    }
}
