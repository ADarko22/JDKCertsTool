package edu.adarko22.process

import edu.adarko22.runner.ProcessExecutor
import edu.adarko22.runner.ProcessResult
import edu.adarko22.utils.*
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

class KeytoolRunnerTest {

    private lateinit var mockPrinter: (String) -> Unit
    private lateinit var capturedOutput: MutableList<String>
    private lateinit var mockExecutor: ProcessExecutor
    private lateinit var mockKeystoreResolver: KeystoreResolver
    private lateinit var keytoolRunner: KeytoolRunner

    @TempDir
    lateinit var tempDir: Path
    private lateinit var mockJdkPath: Path

    private val defaultKeystoreArgs = listOf("-keystore", "path/to/cacerts", "-storepass", "changeit")

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        capturedOutput = mutableListOf()
        mockPrinter = { message -> capturedOutput.add(message) }
        mockExecutor = mockk()
        mockKeystoreResolver = mockk()
        keytoolRunner = KeytoolRunner(
            printer = mockPrinter,
            executor = mockExecutor,
            keystoreResolver = mockKeystoreResolver
        )

        mockJdkPath = tempDir.resolve("jdk_test").apply { createDirectories() }
        mockJdkPath.resolve("bin/keytool")
    }

    @Test
    fun `should verify the correct command string in dry-run mode`() {
        val commandName = "importcert"
        val commandArgs = listOf("-noprompt", "-trustcacerts", "-file", "cert.pem")

        givenKeystoreResolutionReturns(defaultKeystoreArgs)

        keytoolRunner.runCommandWithCacertsResolution(commandName, listOf(mockJdkPath), commandArgs, dryRun = true)

        val expectedCommand = listOf(expectedKeytoolPath()) + commandArgs + defaultKeystoreArgs

        assertAll(
            { verifyKeystoreResolvedOnce() },
            { verify(exactly = 0) { mockExecutor.runCommand(any()) } },
            { assertOutputContains("🛑 Dry run: would run `${expectedCommand.joinToString(" ")}`".blue()) },
            { assertOutputContains("Running $commandName on $mockJdkPath ...") },
            { assertSummary(1, 0) }
        )
    }

    @Test
    @DisplayName("should return false and skip execution when keystoreArgs is null (no cacerts found)")
    fun `should return false and skip execution when keystoreArgs is null`() {
        val commandName = "list"
        val commandArgs = emptyList<String>()

        givenKeystoreResolutionReturns(null)

        keytoolRunner.runCommandWithCacertsResolution(commandName, listOf(mockJdkPath), commandArgs, dryRun = false)

        assertAll(
            { verifyKeystoreResolvedOnce() },
            { verify(exactly = 0) { mockExecutor.runCommand(any()) } },
            { assertOutputContains("❌ No cacerts found. Skipping.".red()) },
            { assertOutputContains("Running $commandName on $mockJdkPath ...") },
            { assertSummary(0, 1) }
        )
    }

    @Test
    @DisplayName("should handle successful command execution (exit code 0)")
    fun `should handle successful command execution`() {
        val commandName = "importcert"
        val commandArgs = listOf("-alias", "mycert")
        val expectedCommand = listOf(expectedKeytoolPath()) + commandArgs + defaultKeystoreArgs

        givenKeystoreResolutionReturns(defaultKeystoreArgs)
        every { mockExecutor.runCommand(expectedCommand) } returns
                ProcessResult("Key imported successfully", "", 0)

        keytoolRunner.runCommandWithCacertsResolution(commandName, listOf(mockJdkPath), commandArgs, dryRun = false)

        assertAll(
            { verifyKeystoreResolvedOnce() },
            { verify(exactly = 1) { mockExecutor.runCommand(expectedCommand) } },
            { assertOutputContains("✅ Success!".green()) },
            { assertOutputContains("Running $commandName on $mockJdkPath ...") },
            { assertSummary(1, 0) }
        )
    }

    @Test
    @DisplayName("should handle failed command execution (non-zero exit code)")
    fun `should handle failed command execution`() {
        val commandName = "list"
        val commandArgs = listOf("-v", "-keystore", "nonexistent.jks")
        val expectedCommand = listOf(expectedKeytoolPath()) + commandArgs + defaultKeystoreArgs

        givenKeystoreResolutionReturns(defaultKeystoreArgs)
        every { mockExecutor.runCommand(expectedCommand) } returns
                ProcessResult("Keytool error: entry not found", "Error details in stderr", 1)

        keytoolRunner.runCommandWithCacertsResolution(commandName, listOf(mockJdkPath), commandArgs, dryRun = false)

        assertAll(
            { verifyKeystoreResolvedOnce() },
            { verify(exactly = 1) { mockExecutor.runCommand(expectedCommand) } },
            { assertOutputContains("❌ Failure (exit code 1)".red()) },
            { assertOutputContains("\tKeytool error: entry not found".yellow()) },
            { assertOutputContains("\tError details in stderr".yellow()) },
            { assertOutputContains("Running $commandName on $mockJdkPath ...") },
            { assertSummary(0, 1) }
        )
    }

    private fun givenKeystoreResolutionReturns(args: List<String>?) {
        every { mockKeystoreResolver.resolve(mockJdkPath) } returns args
    }

    private fun expectedKeytoolPath(): String =
        mockJdkPath.resolve("bin/keytool").absolutePathString()

    private fun verifyKeystoreResolvedOnce() {
        verify(exactly = 1) { mockKeystoreResolver.resolve(mockJdkPath) }
    }

    private fun assertOutputContains(vararg expectedMessages: String) {
        expectedMessages.forEach { msg ->
            assertTrue(
                capturedOutput.any { it.contains(msg) },
                "Expected output to contain: '$msg', but it did not. Actual output: $capturedOutput"
            )
        }
    }

    private fun assertSummary(succeeded: Int, failed: Int) {
        val expectedSummary = "\nSummary: $succeeded succeeded, $failed failed.".blue()
        assertTrue(
            capturedOutput.contains(expectedSummary),
            "Expected summary '$expectedSummary', but got: ${capturedOutput.lastOrNull()}"
        )
    }
}
