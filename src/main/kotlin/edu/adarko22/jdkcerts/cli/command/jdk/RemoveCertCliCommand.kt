package edu.adarko22.jdkcerts.cli.command.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.command.aliasOption
import edu.adarko22.jdkcerts.cli.command.customJdkDirsOption
import edu.adarko22.jdkcerts.cli.command.dryRunOption
import edu.adarko22.jdkcerts.cli.command.keystorePasswordOption
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.usecase.ExecuteKeytoolCommandUseCase
import java.nio.file.Path

/**
 * Command for removing a certificate, by its alias, from JDK cacerts keystore across all the JDK installations discovered.
 */
class RemoveCertCliCommand(
    private val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
    val executeKeytoolCommandCliPresenter: ExecuteKeytoolCommandCliPresenter,
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

        val results = executeKeytoolCommandUseCase.execute(keytoolCommand = command, customJdkDirs, dryRun)
        executeKeytoolCommandCliPresenter.present(results, dryRun)
    }
}
