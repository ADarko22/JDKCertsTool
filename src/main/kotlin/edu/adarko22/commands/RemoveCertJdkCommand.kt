package edu.adarko22.commands

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import edu.adarko22.process.JdkDiscovery
import edu.adarko22.process.KeytoolRunner
import edu.adarko22.utils.bold

/**
 * Command for removing certificates from JDK truststores by alias.
 *
 * This command removes a certificate from the cacerts keystore of all discovered
 * JDK installations using the specified alias. It supports dry-run mode for
 * previewing changes and provides feedback on the removal process for each JDK.
 *
 * The alias must match exactly what was used when the certificate was originally installed.
 */
class RemoveCertJdkCommand(
    private val jdkDiscovery: JdkDiscovery,
    override val printer: (String) -> Unit = ::println,
    override val keytoolRunner: KeytoolRunner = KeytoolRunner(),
) : BaseJdkCommand("remove-cert", "Remove a certificate by alias from all discovered JDKs") {
    private val alias: String
        by option("--alias", help = "Certificate alias to remove")
            .required()

    override fun run() {
        val action = RemoveCertAction(alias, keystorePassword)
        keytoolRunner.runCommandWithCacertsResolution(
            "certificate deletion".bold(),
            discoverAndListJdks(jdkDiscovery),
            action.buildArgs(),
            dryRun,
        )
    }
}
