package edu.adarko22.commands

import com.github.ajalt.clikt.core.parse
import edu.adarko22.process.KeytoolRunner
import edu.adarko22.utils.JdkDiscovery
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class RemoveCertJdkCommandTest {
    private lateinit var mockPrinter: (String) -> Unit
    private lateinit var capturedOutput: MutableList<String>
    private lateinit var mockDiscovery: JdkDiscovery
    private lateinit var mockRunner: KeytoolRunner

    @TempDir
    lateinit var tempDir: Path

    private lateinit var jdkPath: Path

    @BeforeEach
    fun setup() {
        capturedOutput = mutableListOf()
        mockPrinter = { msg -> capturedOutput.add(msg) }

        mockDiscovery = mockk()
        mockRunner = mockk(relaxed = true)

        jdkPath = tempDir.resolve("jdk").apply { Files.createDirectories(this) }
    }

    @Test
    fun `should remove cert by alias if JDKs found`() {
        every { mockDiscovery.discoverJdkHomes(any()) } returns listOf(jdkPath)
        every { mockRunner.runCommandWithCacertsResolution(any(), any(), any(), any()) } just Runs

        val cmd = RemoveCertJdkCommand(mockDiscovery, printer = mockPrinter, keytoolRunner = mockRunner)
        val args = arrayOf("--alias", "test-alias")
        cmd.parse(args)
        cmd.run()

        verify {
            mockRunner.runCommandWithCacertsResolution(
                match { it.contains("certificate deletion") },
                listOf(jdkPath),
                match { it.containsAll(listOf("-delete", "-alias", "test-alias")) },
                false,
            )
        }

        assertTrue(capturedOutput.any { it.contains("Found JDKs") }, "Expected JDK discovery message")
    }

    @Test
    fun `should error if alias is missing`() {
        val cmd = RemoveCertJdkCommand(mockDiscovery, printer = mockPrinter, keytoolRunner = mockRunner)

        assertThrows<Exception> {
            cmd.parse(emptyArray()) // Missing required --alias option
        }
    }
}
