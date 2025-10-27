package edu.adarko22.jdkcerts.clickt.jdk.keytool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.clickt.aliasOption
import edu.adarko22.jdkcerts.clickt.certPathOption
import edu.adarko22.jdkcerts.clickt.customJdkDirsOption
import edu.adarko22.jdkcerts.clickt.dryRunOption
import edu.adarko22.jdkcerts.clickt.keystorePasswordOption
import edu.adarko22.jdkcerts.jdk.discovery.UNIXJdkDiscovery
import edu.adarko22.jdkcerts.jdk.impl.keytool.KeytoolCommand
import edu.adarko22.jdkcerts.output.CommandPrinter
import java.nio.file.Path

/**
 * Command for installing certificates into JDK truststores across multiple JDK installations.
 *
 * This command takes a certificate file (PEM or DER format) and installs it into
 * the cacerts keystore of all discovered JDK installations. It supports dry-run
 * mode for previewing changes and handles different JDK versions automatically.
 *
 * The command validates certificate file existence and provides clear feedback
 * on the installation process for each JDK.
 */
class InstallCertCliktCommand : CliktCommand(name = "install-cert") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()
    private val dryRun: Boolean by dryRunOption()
    private val keystorePassword: String by keystorePasswordOption()
    private val certPath: Path by certPathOption()
    private val alias: String by aliasOption()

    override fun help(context: Context) = "Install certificate in all JDK keystores"

    override fun run() {
        val command =
            KeytoolCommand()
                .addArg("-importcert")
                .addArg("-noprompt")
                .addArg("-trustcacerts")
                .addArg("-alias")
                .addArg(alias)
                .addArg("-file")
                .addArg(certPath.toString())
                .addArg("-storepass")
                .addArg(keystorePassword)
                .withKeystoreResolution()

        val commandExecutor = createKeytoolCommandExecutor()
        commandExecutor.execute(command, customJdkDirs, dryRun)
    }

    fun createKeytoolCommandExecutor(): KeytoolCommandExecutor =
        KeytoolCommandExecutor(
            jdkDiscovery = UNIXJdkDiscovery(),
            output = CommandPrinter(),
        )
}
