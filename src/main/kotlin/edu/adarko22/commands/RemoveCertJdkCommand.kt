package edu.adarko22.commands

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import edu.adarko22.utils.JdkDiscovery
import edu.adarko22.utils.blue

class RemoveCertJdkCommand(
    private val jdkDiscovery: JdkDiscovery,
    override val print: (String) -> Unit = { msg -> println(msg) }
) : BaseJdkCommand(name = "remove-cert", help = "Remove a CA certificate by alias from all discovered JDKs") {

    private val alias: String by option("--alias", help = "Certificate alias to remove").required()
    private val keystorePassword: String by option("--keystore-password", help = "Keystore password").default("changeit")
    private val dryRun: Boolean by option("--dry-run", help = "Preview changes without modifying anything").flag()

    override fun run() {
        val jdkPaths = discoverAndListJdks(jdkDiscovery)
        val keytoolOptions = listOf(
            "-delete",
            "-alias", alias,
            "-storepass", keystorePassword
        )
        runKeyToolCommandWithCacertsResolution("certificate deletion".blue(), jdkPaths, keytoolOptions, dryRun)
    }
}

