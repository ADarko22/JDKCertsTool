package edu.adarko22.commands

import edu.adarko22.utils.JdkDiscover
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.nio.file.Path
class InstallCaCertsTest {

    private val certPath = Path.of(javaClass.getResource("/cert.pem")!!.toURI())
    private val customJdkHome = Path.of(javaClass.getResource("/customJdkHome")!!.toURI())
    private val nonJdkHome = Path.of(javaClass.getResource("/nonJdkHome")!!.toURI())

    private fun runDryRunTest(jdkHome: Path, verifyLogs: (List<String>) -> Unit) {
        val jdkDiscover = mockk<JdkDiscover>()
        every { jdkDiscover.discoverJdkHomes() } returns listOf(jdkHome)

        val loggedMessages = mutableListOf<String>()

        val command = InstallCaCerts(jdkDiscover, print = { msg -> loggedMessages.add(msg) })
        command.parse(arrayOf("--cert", certPath.toString(), "--dry-run"))
        command.run()

        verifyLogs(loggedMessages)
    }

    @Test
    fun `dry-run on customJdkHome logs expected keytool command`() {
        runDryRunTest(customJdkHome) { loggedMessages ->
            val dryRunLog = loggedMessages.find { it.contains("Dry run") }!!
            assertNotNull(dryRunLog, "Dry run log not found")

            assertAll(
                "dry run keytool command contents",
                { assertTrue(dryRunLog.contains(certPath.toString()), "Certificate path missing") },
                { assertTrue(dryRunLog.contains("$customJdkHome/lib/security/cacerts"), "Keystore path missing") },
                {
                    assertTrue(
                        dryRunLog.contains(
                            "keytool -importcert -noprompt -trustcacerts -alias custom-cert -file $certPath -keystore $customJdkHome/lib/security/cacerts"
                        ), "Full keytool command not found"
                    )
                }
            )
        }
    }

    @Test
    fun `dry-run on nonJdkHome logs expected missing cacerts`() {
        runDryRunTest(nonJdkHome) { loggedMessages ->
            val warningLog = loggedMessages.find { it.contains("No cacerts") }
            assertNotNull(warningLog, "Missing warning about absent cacerts")

            assertAll(
                "warning contents",
                { assertTrue(warningLog!!.contains("$nonJdkHome/lib/security/cacerts"), "lib/security/cacerts should be missing") },
                { assertTrue(loggedMessages.none { it.contains("Dry run") }, "No keytool invocation expected") }
            )
        }
    }
}
