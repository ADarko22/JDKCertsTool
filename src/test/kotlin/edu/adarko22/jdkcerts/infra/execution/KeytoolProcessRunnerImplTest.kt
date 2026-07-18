package edu.adarko22.jdkcerts.infra.execution

import edu.adarko22.jdkcerts.core.execution.ProcessResult
import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeystoreInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperationResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.RemoveCertKeytoolCommand
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class KeytoolProcessRunnerImplTest {
    private fun jdk(name: String): Jdk {
        val jdkPath = Path.of("/opt/$name")
        return Jdk(
            path = jdkPath,
            javaInfo = JavaInfo(vendor = "OpenJDK", fullVersion = "17.0.7", major = 17),
            keystoreInfo = KeystoreInfo(keystorePath = jdkPath.resolve("lib/security/cacerts"), cacertsShortcutEnabled = true),
        )
    }

    private fun result(
        exitCode: Int,
        stderr: String = "",
    ): ProcessResult = ProcessResult(stdout = "", stderr = stderr, exitCode = exitCode, dryRunOutput = "")

    @Test
    fun `maps exit code 0 to Success`() =
        runTest {
            val jdk = jdk("jdk17")
            val runner = KeytoolProcessRunnerImpl { _, _ -> result(exitCode = 0) }

            val results = runner.runConcurrently(RemoveCertKeytoolCommand("alias"), listOf(jdk), "pw", dryRun = false)

            assertEquals(1, results.size)
            val success = assertInstanceOf(KeytoolOperationResult.Success::class.java, results.single())
            assertEquals(jdk, success.jdk)
        }

    @Test
    fun `maps non-zero exit code to Failure with descriptive message`() =
        runTest {
            val jdk = jdk("jdk17")
            val runner = KeytoolProcessRunnerImpl { _, _ -> result(exitCode = 1, stderr = "boom") }

            val results = runner.runConcurrently(RemoveCertKeytoolCommand("alias"), listOf(jdk), "pw", dryRun = false)

            val failure = assertInstanceOf(KeytoolOperationResult.Failure::class.java, results.single())
            assertEquals(jdk, failure.jdk)
            assertTrue(failure.errorMessage.contains("exit code 1"), failure.errorMessage)
            assertTrue(failure.errorMessage.contains("boom"), failure.errorMessage)
        }

    @Test
    fun `returns one result per JDK mapped back to its JDK`() =
        runTest {
            val jdk1 = jdk("jdk11")
            val jdk2 = jdk("jdk17")
            // Fail only when targeting jdk17's keytool executable.
            val runner =
                KeytoolProcessRunnerImpl { command, _ ->
                    if (command.first().contains("jdk17")) result(exitCode = 1, stderr = "nope") else result(exitCode = 0)
                }

            val results = runner.runConcurrently(RemoveCertKeytoolCommand("alias"), listOf(jdk1, jdk2), "pw", dryRun = false)

            assertEquals(2, results.size)
            assertInstanceOf(KeytoolOperationResult.Success::class.java, results[0])
            assertEquals(jdk1, results[0].jdk)
            assertInstanceOf(KeytoolOperationResult.Failure::class.java, results[1])
            assertEquals(jdk2, results[1].jdk)
        }

    @Test
    fun `forwards dryRun flag to the process runner`() =
        runTest {
            var observedDryRun: Boolean? = null
            val runner =
                KeytoolProcessRunnerImpl { _, dryRun ->
                    observedDryRun = dryRun
                    result(exitCode = 0)
                }

            runner.runConcurrently(RemoveCertKeytoolCommand("alias"), listOf(jdk("jdk17")), "pw", dryRun = true)

            assertEquals(true, observedDryRun)
        }
}
