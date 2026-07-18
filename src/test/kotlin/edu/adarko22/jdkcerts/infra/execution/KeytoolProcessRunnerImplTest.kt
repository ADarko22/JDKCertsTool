package edu.adarko22.jdkcerts.infra.execution

import edu.adarko22.jdkcerts.core.execution.ProcessResult
import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.FindCertKeytoolQuery
import edu.adarko22.jdkcerts.core.jdk.keytool.model.InstallCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeystoreInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperationResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.RemoveCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.nio.file.Path

@ExtendWith(MockitoExtension::class)
class KeytoolProcessRunnerImplTest {
    @Mock
    private lateinit var processRunner: ProcessRunner

    private lateinit var runner: KeytoolProcessRunnerImpl

    // --- Test Fixtures ---
    private val masterPassword = "changeit"

    private val jdkPath = Path.of("/usr/lib/jvm/java-17")
    private val keytoolPath = Path.of("/usr/lib/jvm/java-17/bin/keytool")
    private val keystorePath = Path.of("/usr/lib/jvm/java-17/lib/security/cacerts")

    private val standardJdk = createDummyJdk(cacertsEnabled = false)
    private val cacertsShortcutJdk = createDummyJdk(cacertsEnabled = true)

    @BeforeEach
    fun setUp() {
        runner = KeytoolProcessRunnerImpl(processRunner)
    }

    @Test
    fun `Install command with standard keystore builds correct args and returns Success`() =
        runTest {
            // Given
            val operation = InstallCertKeytoolCommand(alias = "my-cert", certificateAbsolutePath = "/tmp/cert.pem")
            val successProcessResult = ProcessResult(exitCode = 0, stdout = "Certificate added", stderr = "", dryRunOutput = "")

            whenever(processRunner.runCommand(any(), any())).thenReturn(successProcessResult)

            // When
            val results = runner.runConcurrently(operation, listOf(standardJdk), masterPassword, dryRun = false)

            // Then
            val expectedArgs =
                listOf(
                    keytoolPath.toString(),
                    "-importcert",
                    "-noprompt",
                    "-alias",
                    "my-cert",
                    "-file",
                    "/tmp/cert.pem",
                    "-keystore",
                    keystorePath.toString(),
                    "-storepass",
                    masterPassword,
                )

            verify(processRunner).runCommand(eq(expectedArgs), eq(false))

            assertEquals(1, results.size)
            val result = results.first()
            assertTrue(result is KeytoolOperationResult.Success)
            assertEquals(standardJdk, result.jdk)
            assertEquals(successProcessResult, (result as KeytoolOperationResult.Success).processResult)
        }

    @Test
    fun `Remove command with cacerts shortcut builds correct args and returns Failure on non-zero exit`() =
        runTest {
            // Given
            val operation = RemoveCertKeytoolCommand(alias = "old-cert")
            val failureProcessResult =
                ProcessResult(exitCode = 1, stdout = "", stderr = "keytool error: alias does not exist", dryRunOutput = "")

            whenever(processRunner.runCommand(any(), any())).thenReturn(failureProcessResult)

            // When
            val results = runner.runConcurrently(operation, listOf(cacertsShortcutJdk), masterPassword, dryRun = true)

            // Then
            val expectedArgs =
                listOf(keytoolPath.toString(), "-delete", "-alias", "old-cert", "-cacerts", "-storepass", masterPassword)

            verify(processRunner).runCommand(eq(expectedArgs), eq(true)) // verifies dryRun propagation

            assertEquals(1, results.size)
            val result = results.first()
            assertTrue(result is KeytoolOperationResult.Failure)

            val failure = result as KeytoolOperationResult.Failure
            assertEquals(cacertsShortcutJdk, failure.jdk)
            assertEquals(failureProcessResult, failure.processResult)
            assertTrue(failure.errorMessage.contains("exit code 1"))
            assertTrue(failure.errorMessage.contains("alias does not exist"))
        }

    @Test
    fun `Find command with EXACT_MATCH includes alias in args`() =
        runTest {
            // Given
            val operation = FindCertKeytoolQuery(alias = "my-cert", searchStrategy = SearchStrategy.EXACT_MATCH)
            whenever(processRunner.runCommand(any(), any()))
                .thenReturn(ProcessResult(exitCode = 0, stdout = "", stderr = "", dryRunOutput = ""))

            // When
            runner.runConcurrently(operation, listOf(cacertsShortcutJdk), masterPassword, dryRun = false)

            // Then
            val expectedArgs =
                listOf(keytoolPath.toString(), "-list", "-v", "-alias", "my-cert", "-cacerts", "-storepass", masterPassword)
            verify(processRunner).runCommand(eq(expectedArgs), eq(false))
        }

    @Test
    fun `Find command with REGEX excludes alias from args`() =
        runTest {
            // Given
            val operation = FindCertKeytoolQuery(alias = "ignored-alias", searchStrategy = SearchStrategy.REGEX)
            whenever(processRunner.runCommand(any(), any()))
                .thenReturn(ProcessResult(exitCode = 0, stdout = "", stderr = "", dryRunOutput = ""))

            // When
            runner.runConcurrently(operation, listOf(cacertsShortcutJdk), masterPassword, dryRun = false)

            // Then
            val expectedArgs =
                listOf(keytoolPath.toString(), "-list", "-v", "-cacerts", "-storepass", masterPassword)
            verify(processRunner).runCommand(eq(expectedArgs), eq(false))
        }

    @Test
    fun `Multiple JDKs run concurrently and map results independently`() =
        runTest {
            // Given
            val operation = RemoveCertKeytoolCommand(alias = "my-cert")

            val successResult = ProcessResult(exitCode = 0, stdout = "deleted", stderr = "", dryRunOutput = "")
            val failureResult = ProcessResult(exitCode = 255, stdout = "", stderr = "permission denied", dryRunOutput = "")

            // Mock different responses based on the arguments that will be generated for each JDK type
            whenever(processRunner.runCommand(any(), any()))
                .thenAnswer { invocation ->
                    val args = invocation.arguments[0] as List<*>
                    if (args.contains("-cacerts")) successResult else failureResult
                }

            // When
            val results =
                runner.runConcurrently(operation, listOf(cacertsShortcutJdk, standardJdk), masterPassword, dryRun = false)

            // Then
            assertEquals(2, results.size)
            verify(processRunner, times(2)).runCommand(any(), any())

            // Verify result mapping for cacerts shortcut JDK
            val cacertsResult = results.find { it.jdk == cacertsShortcutJdk }
            assertTrue(cacertsResult is KeytoolOperationResult.Success)

            // Verify result mapping for standard JDK
            val standardResult = results.find { it.jdk == standardJdk }
            assertTrue(standardResult is KeytoolOperationResult.Failure)
        }

    // --- Helper Methods ---

    private fun createDummyJdk(cacertsEnabled: Boolean): Jdk {
        // Adjust this depending on your actual Jdk / KeystoreInfo constructors
        return Jdk(
            path = jdkPath,
            javaInfo = JavaInfo("Vendor", "17.0", 17),
            keystoreInfo = KeystoreInfo(keystorePath = keystorePath, cacertsShortcutEnabled = cacertsEnabled),
        )
    }
}
