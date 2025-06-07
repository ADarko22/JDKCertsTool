package edu.adarko22.commands

import com.github.ajalt.clikt.parameters.options.*
import edu.adarko22.utils.JdkDiscovery
import edu.adarko22.utils.*
import java.nio.file.Files
import java.nio.file.Path

class InstallCertsJdkCommand(
    private val jdkDiscovery: JdkDiscovery,
    override val print: (String) -> Unit = { msg -> println(msg) }
) : BaseJdkCommand(name = "install-cert", help = "Install a CA certificate into all discovered JDKs") {

    private val certPath: Path by option("--cert", help = "Path to the certificate file").convert { it.toPath() }
        .required()
    private val keystorePassword: String by option(
        "--keystore-password",
        help = "Keystore password"
    ).default("changeit")
    private val alias: String by option("--alias", help = "Certificate alias").default("custom-cert")
    private val dryRun: Boolean by option("--dry-run", help = "Preview changes without modifying anything").flag()

    override fun run() {
        if (!Files.exists(certPath)) {
            print("❌ Certificate not found: $certPath".red())

            if (dryRun)
                println("Dry run: Would have stopped, but continuing execution for simulation purposes.".blue())
            else return
        }

        val jdkPaths = discoverAndListJdks(jdkDiscovery)
        val keytoolOptions = listOf(
            "-importcert", "-noprompt", "-trustcacerts",
            "-alias", alias,
            "-file", certPath.toString(),
            "-storepass", keystorePassword
        )
        runKeyToolCommandWithCacertsResolution("certificate installation".bold(), jdkPaths, keytoolOptions, dryRun)
    }
}