package edu.adarko22.commands

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import edu.adarko22.utils.JdkDiscover
import edu.adarko22.utils.*
import java.nio.file.Files

class RemoveCaCertCommand(
    private val jdkDiscovery: JdkDiscover,
    override val print: (String) -> Unit = { msg -> println(msg) }
) : BaseJdkCommand(name = "remove-cert", help = "Remove a CA certificate by alias from all discovered JDKs") {

    private val alias: String by option("--alias", help = "Certificate alias to remove").required()
    private val keystorePassword: String by option("--keystore-password", help = "Keystore password").default("changeit")
    private val dryRun: Boolean by option("--dry-run", help = "Preview changes without modifying anything").flag()

    override fun run() {
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
                "keytool", "-delete",
                "-alias", alias,
                "-keystore", cacerts.toString(),
                "-storepass", keystorePassword
            )

            print("🗑️ Removing alias '$alias' from $cacerts")

            if (dryRun) {
                print("🛑 Dry run: would run `${cmd.joinToString(" ")}`".blue())
                successes++
                return@forEach
            }

            try {
                val exitCode = ProcessBuilder(cmd).inheritIO().start().waitFor()
                if (exitCode == 0) {
                    print("✅ Removed alias '$alias' from $jdk".green())
                    successes++
                } else {
                    print("❌ Failed to remove alias from $jdk (exit code $exitCode)".red())
                    failures++
                }
            } catch (e: Exception) {
                print("❌ Error removing alias from $jdk: ${e.message}".red())
                failures++
            }
        }

        print("\nSummary: $successes succeeded, $failures failed.")
    }
}
