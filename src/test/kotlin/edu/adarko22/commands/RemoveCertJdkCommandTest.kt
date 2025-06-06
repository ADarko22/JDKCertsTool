package edu.adarko22.commands

import edu.adarko22.utils.JdkDiscovery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.nio.file.Path

class RemoveCertJdkCommandTest {

    private val customJdkHome = Path.of(javaClass.getResource("/customJdkHome")!!.toURI())
    private val nonJdkHome = Path.of(javaClass.getResource("/nonJdkHome")!!.toURI())

    private fun runDryRunTestForJdk(
        jdkHome: Path,
        verifyLogs: (List<String>) -> Unit
    ) {
        val jdkDiscovery = mockk<JdkDiscovery>()
        every { jdkDiscovery.discoverJdkHomes(any()) } returns listOf(jdkHome)

        val logs = mutableListOf<String>()

        val command = RemoveCertJdkCommand(jdkDiscovery, print = { msg -> logs.add(msg) })
        command.parse(arrayOf("--alias", "custom-cert", "--dry-run"))
        command.run()

        verifyLogs(logs)
    }

    @Test
    fun `dry-run on customJdkHome logs expected keytool command`() {
        runDryRunTestForJdk(customJdkHome) { logs ->
            val removalLog = logs.find { it.contains("Removing alias") }
            val dryRunLog = logs.find { it.contains("Dry run") }

            assertAll(
                "Dry-run logs",
                { assertNotNull(removalLog, "Removal log missing") },
                { assertTrue(removalLog!!.contains(customJdkHome.resolve("lib/security/cacerts").toString()), "Removal log missing cacerts path") },
                { assertNotNull(dryRunLog, "Dry run log missing") },
                {
                    assertTrue(
                        dryRunLog!!.contains("keytool -delete -alias custom-cert -keystore $customJdkHome/lib/security/cacerts"),
                        "Dry run command does not match expected keytool command"
                    )
                }
            )
        }
    }

    @Test
    fun `dry-run on nonJdkHome logs warning about missing cacerts and no keytool command`() {
        runDryRunTestForJdk(nonJdkHome) { logs ->
            val warningLog = logs.find { it.contains("No cacerts") }

            assertAll(
                "Missing cacerts logs",
                { assertNotNull(warningLog, "Expected warning log for missing cacerts") },
                { assertTrue(warningLog!!.contains("$nonJdkHome/lib/security/cacerts"), "Warning log missing correct cacerts path") },
                { assertTrue(logs.none { it.contains("Removing alias") }, "Removal should not be attempted") },
                { assertTrue(logs.none { it.contains("Dry run") }, "Dry run keytool command should not be logged") }
            )
        }
    }
}
