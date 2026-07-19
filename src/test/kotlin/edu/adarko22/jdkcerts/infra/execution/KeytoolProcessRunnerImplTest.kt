package edu.adarko22.jdkcerts.infra.execution

import edu.adarko22.jdkcerts.core.execution.KeytoolProcessResult
import edu.adarko22.jdkcerts.core.execution.ProcessResult
import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.FindCertKeytoolQuery
import edu.adarko22.jdkcerts.core.jdk.keytool.model.InstallCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeystoreInfo
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
import org.mockito.kotlin.never
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
    fun `Install command with standard keystore builds correct args and returns Executed`() =
        runTest {
            // Given
            val operation = InstallCertKeytoolCommand(alias = "my-cert", certificateAbsolutePath = "/tmp/cert.pem")
            whenever(processRunner.runCommand(any())).thenReturn(ProcessResult(stdout = "Certificate added", stderr = "", exitCode = 0))

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
            verify(processRunner).runCommand(eq(expectedArgs))

            val result = results.single()
            assertTrue(result is KeytoolProcessResult.Executed)
            result as KeytoolProcessResult.Executed
            assertEquals(standardJdk, result.jdk)
            assertEquals(0, result.exitCode)
            assertEquals("Certificate added", result.stdout)
        }

    @Test
    fun `Non-zero exit is reported verbatim as Executed without a verdict`() =
        runTest {
            // Given
            val operation = RemoveCertKeytoolCommand(alias = "old-cert")
            whenever(processRunner.runCommand(any()))
                .thenReturn(ProcessResult(stdout = "", stderr = "alias does not exist", exitCode = 1))

            // When
            val results = runner.runConcurrently(operation, listOf(cacertsShortcutJdk), masterPassword, dryRun = false)

            // Then
            val expectedArgs =
                listOf(keytoolPath.toString(), "-delete", "-alias", "old-cert", "-cacerts", "-storepass", masterPassword)
            verify(processRunner).runCommand(eq(expectedArgs))

            val result = results.single()
            assertTrue(result is KeytoolProcessResult.Executed)
            result as KeytoolProcessResult.Executed
            assertEquals(1, result.exitCode)
            assertEquals("alias does not exist", result.stderr)
        }

    @Test
    fun `Dry run returns a preview and never touches the process runner`() =
        runTest {
            // Given
            val operation = RemoveCertKeytoolCommand(alias = "old-cert")

            // When
            val results = runner.runConcurrently(operation, listOf(cacertsShortcutJdk), masterPassword, dryRun = true)

            // Then
            verify(processRunner, never()).runCommand(any())

            val expectedPreview =
                listOf(keytoolPath.toString(), "-delete", "-alias", "old-cert", "-cacerts", "-storepass", masterPassword)
                    .joinToString(" ")
            val result = results.single()
            assertTrue(result is KeytoolProcessResult.DryRun)
            assertEquals(expectedPreview, (result as KeytoolProcessResult.DryRun).previewCommand)
        }

    @Test
    fun `Find command with EXACT_MATCH includes alias in args`() =
        runTest {
            // Given
            val operation = FindCertKeytoolQuery(alias = "my-cert", searchStrategy = SearchStrategy.EXACT_MATCH)
            whenever(processRunner.runCommand(any())).thenReturn(ProcessResult(stdout = "", stderr = "", exitCode = 0))

            // When
            runner.runConcurrently(operation, listOf(cacertsShortcutJdk), masterPassword, dryRun = false)

            // Then
            val expectedArgs =
                listOf(keytoolPath.toString(), "-list", "-v", "-alias", "my-cert", "-cacerts", "-storepass", masterPassword)
            verify(processRunner).runCommand(eq(expectedArgs))
        }

    @Test
    fun `Find command with REGEX excludes alias from args`() =
        runTest {
            // Given
            val operation = FindCertKeytoolQuery(alias = "ignored-alias", searchStrategy = SearchStrategy.REGEX)
            whenever(processRunner.runCommand(any())).thenReturn(ProcessResult(stdout = "", stderr = "", exitCode = 0))

            // When
            runner.runConcurrently(operation, listOf(cacertsShortcutJdk), masterPassword, dryRun = false)

            // Then
            val expectedArgs = listOf(keytoolPath.toString(), "-list", "-v", "-cacerts", "-storepass", masterPassword)
            verify(processRunner).runCommand(eq(expectedArgs))
        }

    @Test
    fun `Multiple JDKs run concurrently and map outcomes independently`() =
        runTest {
            // Given
            val operation = RemoveCertKeytoolCommand(alias = "my-cert")
            val successResult = ProcessResult(stdout = "deleted", stderr = "", exitCode = 0)
            val failureResult = ProcessResult(stdout = "", stderr = "permission denied", exitCode = 255)

            whenever(processRunner.runCommand(any()))
                .thenAnswer { invocation ->
                    val args = invocation.arguments[0] as List<*>
                    if (args.contains("-cacerts")) successResult else failureResult
                }

            // When
            val results =
                runner.runConcurrently(operation, listOf(cacertsShortcutJdk, standardJdk), masterPassword, dryRun = false)

            // Then
            assertEquals(2, results.size)
            verify(processRunner, times(2)).runCommand(any())

            val cacertsResult = results.find { it.jdk == cacertsShortcutJdk }
            assertTrue(cacertsResult is KeytoolProcessResult.Executed)
            assertEquals(0, (cacertsResult as KeytoolProcessResult.Executed).exitCode)

            val standardResult = results.find { it.jdk == standardJdk }
            assertTrue(standardResult is KeytoolProcessResult.Executed)
            assertEquals(255, (standardResult as KeytoolProcessResult.Executed).exitCode)
        }

    // --- Helper Methods ---

    private fun createDummyJdk(cacertsEnabled: Boolean): Jdk =
        Jdk(
            path = jdkPath,
            javaInfo = JavaInfo("Vendor", "17.0", 17),
            keystoreInfo = KeystoreInfo(keystorePath = keystorePath, cacertsShortcutEnabled = cacertsEnabled),
        )
}
