package edu.adarko22.commands

import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import edu.adarko22.process.JdkDiscovery
import edu.adarko22.process.KeytoolRunner
import edu.adarko22.utils.blue
import edu.adarko22.utils.bold
import edu.adarko22.utils.red
import java.nio.file.Files
import java.nio.file.Path

class InstallCertsJdkCommand(
    private val jdkDiscovery: JdkDiscovery,
    override val printer: (String) -> Unit = ::println,
    override val keytoolRunner: KeytoolRunner = KeytoolRunner(),
) : BaseJdkCommand("install-cert", "Install a certificate into all discovered JDKs") {
    private val certPath: Path
        by option("--cert", help = "Path to the certificate file")
            .convert { it.toPath() }
            .required()

    private val alias: String
        by option("--alias", help = "Certificate alias")
            .default("custom-cert")

    override fun run() {
        if (!Files.exists(certPath)) {
            printer("❌ Certificate not found: $certPath".red())
            if (dryRun) {
                printer("Dry run: Continuing anyway.".blue())
            } else {
                return
            }
        }

        val action = InstallCertAction(certPath, alias, keystorePassword)

        keytoolRunner.runCommandWithCacertsResolution(
            "certificate installation".bold(),
            discoverAndListJdks(jdkDiscovery),
            action.buildArgs(),
            dryRun,
        )
    }
}
