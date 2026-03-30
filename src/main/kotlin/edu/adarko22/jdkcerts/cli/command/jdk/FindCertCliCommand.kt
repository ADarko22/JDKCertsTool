package edu.adarko22.jdkcerts.cli.command.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.command.aliasOption
import edu.adarko22.jdkcerts.cli.command.customJdkDirsOption
import edu.adarko22.jdkcerts.cli.command.keystorePasswordOption
import edu.adarko22.jdkcerts.cli.command.verboseOption
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import edu.adarko22.jdkcerts.core.jdk.keytool.usecase.FindKeytoolCertificateUseCase
import java.nio.file.Path

/**
 * Command for finding a certificate, by its alias, from JDK cacerts keystore across all the JDK installations discovered.
 */
class FindCertCliCommand(
    val findKeytoolCertificateUseCase: FindKeytoolCertificateUseCase,
    private val findCertCliPresenter: FindCertCliPresenter,
) : CliktCommand(name = "find-cert") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()
    private val keystorePassword: String by keystorePasswordOption()
    private val alias: String by aliasOption()

    private val verbose: Boolean by verboseOption()

    override fun help(context: Context) = "Find certificate across all JDK keystores"

    override fun run() {
        val results =
            findKeytoolCertificateUseCase.execute(
                alias,
                keystorePassword,
                customJdkDirs,
                SearchStrategy.EXACT_MATCH,
            )
        findCertCliPresenter.present(results, verbose, alias)
    }
}
