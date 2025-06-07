package edu.adarko22.commands

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import edu.adarko22.process.KeytoolRunner
import edu.adarko22.utils.JdkDiscovery
import edu.adarko22.utils.bold

class RemoveCertJdkCommand(
    private val jdkDiscovery: JdkDiscovery,
    override val printer: (String) -> Unit = ::println,
    override val keytoolRunner: KeytoolRunner = KeytoolRunner()
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
            dryRun
        )
    }
}
