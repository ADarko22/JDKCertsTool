package edu.adarko22.jdkcerts.cli.command.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.command.aliasOption
import edu.adarko22.jdkcerts.cli.command.certPathOption
import edu.adarko22.jdkcerts.cli.command.customJdkDirsOption
import edu.adarko22.jdkcerts.cli.command.dryRunOption
import edu.adarko22.jdkcerts.cli.command.jdk.keytool.KeytoolCommand
import edu.adarko22.jdkcerts.cli.command.jdk.keytool.KeytoolCommandExecutor
import edu.adarko22.jdkcerts.cli.command.keystorePasswordOption
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
class InstallCertCliCommand(
    val keytoolCommandExecutor: KeytoolCommandExecutor,
) : CliktCommand(name = "install-cert") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()
    private val dryRun: Boolean by dryRunOption()
    private val keystorePassword: String by keystorePasswordOption()
    private val certPath: Path by certPathOption()
    private val alias: String by aliasOption()

    override fun help(context: Context) = "Install certificate in all JDK keystores"

    override fun run() {
        val command =
            KeytoolCommand
                .Builder()
                .addArg("-importcert")
                .addArg("-noprompt")
                .addArg("-trustcacerts")
                .addArg("-alias $alias")
                .addArg("-file $certPath")
                .addArg("-storepass $keystorePassword")
                .withKeystoreResolution()
                .build()

        keytoolCommandExecutor.execute(command, customJdkDirs, dryRun)
    }
}
