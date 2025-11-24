package edu.adarko22.jdkcerts.cli.command.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.command.aliasOption
import edu.adarko22.jdkcerts.cli.command.customJdkDirsOption
import edu.adarko22.jdkcerts.cli.command.dryRunOption
import edu.adarko22.jdkcerts.cli.command.keystorePasswordOption
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
import java.nio.file.Path

/**
 * Command for removing certificates from JDK keystore by alias.
 *
 * This command removes a certificate from the cacerts keystore of all discovered
 * JDK installations using the specified alias. It supports dry-run mode for
 * previewing changes and provides feedback on the removal process for each JDK.
 *
 * The alias must match exactly what was used when the certificate was originally installed.
 */
class RemoveCertCliCommand(
    val keytoolCliPresenter: KeytoolCliPresenter,
) : CliktCommand(name = "remove-cert") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()
    private val dryRun: Boolean by dryRunOption()
    private val keystorePassword: String by keystorePasswordOption()
    private val alias: String by aliasOption()

    override fun help(context: Context) = "Remove certificate from all JDK keystores"

    override fun run() {
        val command =
            KeytoolCommand
                .Builder()
                .addArg("-delete")
                .addArg("-alias $alias")
                .addArg("-storepass $keystorePassword")
                .withKeystoreResolution()
                .build()

        keytoolCliPresenter.present(command, customJdkDirs, dryRun)
    }
}
