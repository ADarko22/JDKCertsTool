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

    private fun runDryRunTest(jdkHome: Path, verifyLogs: (List<String>) -> Unit) {
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
        runDryRunTest(customJdkHome) { logs ->
            val dryRunLog = logs.find { it.contains("Dry run") }!!
            assertNotNull(dryRunLog, "Dry run log not found")

            assertAll(
                "dry run keytool command contents",
                { assertTrue(dryRunLog.contains("$customJdkHome/lib/security/cacerts"), "Keystore path missing") },
                {
                    assertTrue(
                        dryRunLog.contains(
                            "keytool -delete -alias custom-cert -storepass changeit -keystore $customJdkHome/lib/security/cacerts"
                        ), "Full keytool command not found"
                    )
                }
            )
        }
    }

    @Test
    fun `dry-run on nonJdkHome logs warning about missing cacerts and no keytool command`() {
        runDryRunTest(nonJdkHome) { logs ->
            val warningLog = logs.find { it.contains("No cacerts") }
            assertNotNull(warningLog, "Missing warning about absent cacerts")

            assertAll(
                "warning contents",
                { assertTrue(warningLog!!.contains("No cacerts found. Skipping."), "cacerts should be missing") },
                { assertTrue(logs.none { it.contains("keytool") }, "No keytool invocation expected") }
            )
        }
    }
}
