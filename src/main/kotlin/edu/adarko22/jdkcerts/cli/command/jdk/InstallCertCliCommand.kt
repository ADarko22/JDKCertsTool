package edu.adarko22.jdkcerts.cli.command.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.command.aliasOption
import edu.adarko22.jdkcerts.cli.command.certPathOption
import edu.adarko22.jdkcerts.cli.command.customJdkPathsOption
import edu.adarko22.jdkcerts.cli.command.dryRunOption
import edu.adarko22.jdkcerts.cli.command.keystorePasswordOption
import edu.adarko22.jdkcerts.core.jdk.keytool.model.ExecutionContext
import edu.adarko22.jdkcerts.core.jdk.keytool.model.InstallCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.usecase.ExecuteKeytoolCommandUseCase
import kotlinx.coroutines.runBlocking
import java.nio.file.Path

/**
 * Command for installing certificates into JDK cacerts keystore across all the JDK installations discovered.
 */
class InstallCertCliCommand(
    private val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
    val keytoolCommandResultsCliPresenter: KeytoolCommandResultsCliPresenter,
) : CliktCommand(name = "install-cert") {
    private val customJdkPaths: List<Path> by customJdkPathsOption()
    private val dryRun: Boolean by dryRunOption()
    private val keystorePassword: String by keystorePasswordOption()
    private val certPath: Path by certPathOption()
    private val alias: String by aliasOption()

    override fun help(context: Context) = "Install certificate across all JDK keystores"

    override fun run() {
        val results =
            runBlocking {
                val installCertKeytoolCommand = InstallCertKeytoolCommand(alias, certPath.toAbsolutePath().toString())
                val executionContext = ExecutionContext(customJdkPaths, keystorePassword, dryRun)
                executeKeytoolCommandUseCase.execute(installCertKeytoolCommand, executionContext)
            }
        keytoolCommandResultsCliPresenter.present(results)
    }
}
