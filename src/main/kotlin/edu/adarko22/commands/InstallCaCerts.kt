package edu.adarko22.commands

import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.options.*
import edu.adarko22.utils.JdkDiscover
import java.nio.file.Files
import java.nio.file.Path

class InstallCaCerts(
    private val jdkDiscovery: JdkDiscover,
    override val print: (String) -> Unit = { msg -> println(msg) }
) : BaseJdkCommand(name = "install-cert", help = "Install a CA certificate into all discovered JDKs") {

    private val certPath: Path by option("--cert", help = "Path to the certificate file").convert { it.toPath() }.required()
    private val keystorePassword: String by option("--keystore-password", help = "Keystore password").default("changeit")
    private val alias: String by option("--alias", help = "Certificate alias").default("custom-cert")
    private val dryRun: Boolean by option("--dry-run", help = "Preview changes without modifying anything").flag()

    override fun run() {
        if (!Files.exists(certPath)) {
            print("❌ Certificate not found: $certPath".red())
            return
        }

        val jdkPaths = discoverAndListJdks(jdkDiscovery)
        var successes = 0
        var failures = 0

        jdkPaths.forEach { jdk ->
            val cacerts = jdk.resolve("lib/security/cacerts")
            if (!Files.exists(cacerts)) {
                print("⚠️ No cacerts at $cacerts, skipping.".yellow())
                return@forEach
            }

            val cmd = listOf(
                "keytool", "-importcert", "-noprompt", "-trustcacerts",
                "-alias", alias,
                "-file", certPath.toString(),
                "-keystore", cacerts.toString(),
                "-storepass", keystorePassword
            )

            print("🔐 Importing into $cacerts")

            if (dryRun) {
                print("🛑 Dry run: would run `${cmd.joinToString(" ")}`".blue())
                successes++
                return@forEach
            }

            try {
                val exitCode = ProcessBuilder(cmd).inheritIO().start().waitFor()
                if (exitCode == 0) {
                    print("✅ Success for $jdk".green())
                    successes++
                } else {
                    print("❌ Failed to import for $jdk (exit code $exitCode)".red())
                    failures++
                }
            } catch (e: Exception) {
                print("❌ Error importing for $jdk: ${e.message}".red())
                failures++
            }
        }

        print("\nSummary: $successes succeeded, $failures failed.")
    }
}
