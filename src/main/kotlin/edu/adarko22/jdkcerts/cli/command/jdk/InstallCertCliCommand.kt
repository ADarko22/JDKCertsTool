package edu.adarko22.jdkcerts.cli.command.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.command.aliasOption
import edu.adarko22.jdkcerts.cli.command.certPathOption
import edu.adarko22.jdkcerts.cli.command.customJdkDirsOption
import edu.adarko22.jdkcerts.cli.command.dryRunOption
import edu.adarko22.jdkcerts.cli.command.keystorePasswordOption
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
import java.nio.file.Path

/**
 * Command for installing certificates into JDK cacerts keystore across all the JDK installations discovered.
 *
 */
class InstallCertCliCommand(
    val keytoolCliPresenter: KeytoolCliPresenter,
) : CliktCommand(name = "install-cert") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()
    private val dryRun: Boolean by dryRunOption()
    private val keystorePassword: String by keystorePasswordOption()
    private val certPath: Path by certPathOption()
    private val alias: String by aliasOption()

    override fun help(context: Context) = "Install certificate across all JDK keystores"

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

        keytoolCliPresenter.present(command, customJdkDirs, dryRun)
    }
}
