package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.execution.KeytoolProcessResult
import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.ExecutionContext
import edu.adarko22.jdkcerts.core.jdk.keytool.model.InstallCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeystoreInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.RemoveCertKeytoolCommand
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.nio.file.Path

class ExecuteKeytoolCommandUseCaseTest {
    private val jdk = jdk("jdk17")
    private val discoverJdks = mockk<DiscoverJdksUseCase>()
    private val processRunner = mockk<KeytoolProcessRunner>()
    private val useCase = ExecuteKeytoolCommandUseCase(discoverJdks, processRunner)
    private val context = ExecutionContext(masterPassword = "pw")

    private suspend fun run(
        command: KeytoolCommand,
        outcomes: List<KeytoolProcessResult>,
    ): List<KeytoolCommandResult> {
        coEvery { discoverJdks.discover(any()) } returns outcomes.map { it.jdk }
        coEvery { processRunner.runConcurrently(any(), any(), any(), any()) } returns outcomes
        return useCase.execute(command, context)
    }

    @Test
    fun `exit zero maps to Success`() =
        runTest {
            val results = run(RemoveCertKeytoolCommand("my-cert"), listOf(executed(exitCode = 0)))

            assertInstanceOf(KeytoolCommandResult.Success::class.java, results.single())
        }

    @Test
    fun `dry run outcome maps to DryRun result`() =
        runTest {
            val results = run(RemoveCertKeytoolCommand("my-cert"), listOf(KeytoolProcessResult.DryRun(jdk, "keytool -delete")))

            val dryRun = assertInstanceOf(KeytoolCommandResult.DryRun::class.java, results.single())
            assertEquals("keytool -delete", dryRun.previewCommand)
        }

    @Test
    fun `wrong password maps to WrongPassword failure`() =
        runTest {
            val results =
                run(RemoveCertKeytoolCommand("my-cert"), listOf(executed(exitCode = 1, stderr = "keystore password was incorrect")))

            assertInstanceOf(KeytoolCommandResult.Failure.WrongPassword::class.java, results.single())
        }

    @Test
    fun `taken alias on install maps to AliasAlreadyExists carrying the command alias`() =
        runTest {
            val results =
                run(
                    InstallCertKeytoolCommand("my-cert", "/tmp/cert.pem"),
                    listOf(executed(exitCode = 1, stderr = "Certificate not imported, alias <my-cert> already exists")),
                )

            val failure = assertInstanceOf(KeytoolCommandResult.Failure.AliasAlreadyExists::class.java, results.single())
            assertEquals("my-cert", failure.alias)
        }

    @Test
    fun `duplicate certificate maps to CertificateAlreadyExists with conflicting alias`() =
        runTest {
            val results =
                run(
                    InstallCertKeytoolCommand("my-cert", "/tmp/cert.pem"),
                    listOf(executed(exitCode = 1, stderr = "Certificate already exists in keystore under alias <other-ca>")),
                )

            val failure = assertInstanceOf(KeytoolCommandResult.Failure.CertificateAlreadyExists::class.java, results.single())
            assertEquals("other-ca", failure.conflictingAlias)
        }

    @Test
    fun `missing alias on delete maps to AliasNotFound`() =
        runTest {
            val results =
                run(RemoveCertKeytoolCommand("gone"), listOf(executed(exitCode = 1, stderr = "Alias <gone> does not exist")))

            val failure = assertInstanceOf(KeytoolCommandResult.Failure.AliasNotFound::class.java, results.single())
            assertEquals("gone", failure.alias)
        }

    @Test
    fun `unclassified failure maps to Unknown carrying the exit code`() =
        runTest {
            val results = run(RemoveCertKeytoolCommand("my-cert"), listOf(executed(exitCode = 9, stderr = "boom")))

            val failure = assertInstanceOf(KeytoolCommandResult.Failure.Unknown::class.java, results.single())
            assertEquals(9, failure.exitCode)
        }

    @Test
    fun `preserves one result per JDK for mixed outcomes`() =
        runTest {
            val jdk2 = jdk("jdk11")
            val outcomes = listOf(executed(jdk, exitCode = 0), executed(jdk2, exitCode = 9, stderr = "boom"))

            val results = run(RemoveCertKeytoolCommand("my-cert"), outcomes)

            assertEquals(2, results.size)
            assertInstanceOf(KeytoolCommandResult.Success::class.java, results[0])
            assertInstanceOf(KeytoolCommandResult.Failure.Unknown::class.java, results[1])
        }

    private fun executed(
        target: Jdk = jdk,
        exitCode: Int,
        stdout: String = "",
        stderr: String = "",
    ): KeytoolProcessResult.Executed = KeytoolProcessResult.Executed(target, exitCode, stdout, stderr)
}

private fun jdk(name: String): Jdk {
    val jdkPath = Path.of("/opt/$name")
    return Jdk(
        path = jdkPath,
        javaInfo = JavaInfo(vendor = "OpenJDK", fullVersion = "17.0.7", major = 17),
        keystoreInfo = KeystoreInfo(keystorePath = jdkPath.resolve("lib/security/cacerts"), cacertsShortcutEnabled = true),
    )
}
