package edu.adarko22.jdkcerts.core.jdk.keytool.model

import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path

class KeytoolOperationTest {
    private fun jdk(cacertsShortcutEnabled: Boolean): Jdk {
        val jdkPath = Path.of("/opt/jdk17")
        return Jdk(
            path = jdkPath,
            javaInfo = JavaInfo(vendor = "OpenJDK", fullVersion = "17.0.7", major = 17),
            keystoreInfo =
                KeystoreInfo(
                    keystorePath = jdkPath.resolve("lib/security/cacerts"),
                    cacertsShortcutEnabled = cacertsShortcutEnabled,
                ),
        )
    }

    @Test
    fun `FindCertKeytoolQuery getArgs uses native alias option for EXACT_MATCH`() {
        val query = FindCertKeytoolQuery(alias = "my-cert", searchStrategy = SearchStrategy.EXACT_MATCH)

        assertEquals(listOf("-list", "-v", "-alias", "my-cert"), query.getArgs())
    }

    @Test
    fun `FindCertKeytoolQuery getArgs lists all entries for REGEX`() {
        val query = FindCertKeytoolQuery(alias = "prefix.*", searchStrategy = SearchStrategy.REGEX)

        assertEquals(listOf("-list", "-v"), query.getArgs())
    }

    @Test
    fun `FindCertKeytoolQuery getArgs lists all entries for CLOSEST_MATCH`() {
        val query = FindCertKeytoolQuery(alias = "my-cert", searchStrategy = SearchStrategy.CLOSEST_MATCH)

        assertEquals(listOf("-list", "-v"), query.getArgs())
    }

    @Test
    fun `InstallCertKeytoolCommand getArgs builds importcert invocation`() {
        val command = InstallCertKeytoolCommand(alias = "my-cert", certificateAbsolutePath = "/tmp/cert.pem")

        assertEquals(
            listOf("-importcert", "-noprompt", "-trustcacerts", "-alias", "my-cert", "-file", "/tmp/cert.pem"),
            command.getArgs(),
        )
    }

    @Test
    fun `RemoveCertKeytoolCommand getArgs builds delete invocation`() {
        val command = RemoveCertKeytoolCommand(alias = "my-cert")

        assertEquals(listOf("-delete", "-alias", "my-cert"), command.getArgs())
    }

    @Test
    fun `buildCommand uses cacerts shortcut when enabled`() {
        val command = RemoveCertKeytoolCommand(alias = "my-cert")

        val fullCommand = command.buildCommand(jdk(cacertsShortcutEnabled = true), keystorePassword = "secret")

        assertEquals(
            listOf("/opt/jdk17/bin/keytool", "-delete", "-alias", "my-cert", "-cacerts", "-storepass", "secret"),
            fullCommand,
        )
    }

    @Test
    fun `buildCommand uses explicit keystore path when cacerts shortcut disabled`() {
        val query = FindCertKeytoolQuery(alias = "my-cert", searchStrategy = SearchStrategy.EXACT_MATCH)

        val fullCommand = query.buildCommand(jdk(cacertsShortcutEnabled = false), keystorePassword = "secret")

        assertEquals(
            listOf(
                "/opt/jdk17/bin/keytool",
                "-list",
                "-v",
                "-alias",
                "my-cert",
                "-keystore",
                "/opt/jdk17/lib/security/cacerts",
                "-storepass",
                "secret",
            ),
            fullCommand,
        )
    }
}
