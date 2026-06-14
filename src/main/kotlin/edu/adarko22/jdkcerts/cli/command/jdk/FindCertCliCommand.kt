package edu.adarko22.jdkcerts.cli.command.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import edu.adarko22.jdkcerts.cli.command.aliasOption
import edu.adarko22.jdkcerts.cli.command.customJdkDirsOption
import edu.adarko22.jdkcerts.cli.command.keystorePasswordOption
import edu.adarko22.jdkcerts.cli.command.verboseOption
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import edu.adarko22.jdkcerts.core.jdk.keytool.usecase.FindKeytoolCertificateUseCase
import kotlinx.coroutines.runBlocking
import java.nio.file.Path

/**
 * Find certificate by alias across all JDK keystores.
 *
 * Default searches for exact alias match. Use `--regex` or `--closest-match` for other strategies.
 */
class FindCertCliCommand(
    val findKeytoolCertificateUseCase: FindKeytoolCertificateUseCase,
    private val findCertCliPresenter: FindCertCliPresenter,
) : CliktCommand(name = "find-cert") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()
    private val keystorePassword: String by keystorePasswordOption()
    private val alias: String by aliasOption()
    private val verbose: Boolean by verboseOption()

    private val useRegex: Boolean by option(
        "--regex",
        help = "Search by regex pattern instead of exact match.",
    ).flag()

    private val useClosestMatch: Boolean by option(
        "--closest-match",
        help = "Search by closest match (fuzzy matching for typos).",
    ).flag()

    override fun help(context: Context) =
        "Find certificate by alias across JDK keystores (default: exact match, use --regex or --closest-match for other strategies)"

    override fun run() {
        // Determine search strategy from flags
        val searchStrategy =
            when {
                useRegex && useClosestMatch -> {
                    echo(
                        "Error: Cannot use both --regex and --closest-match. Please choose one.",
                        err = true,
                    )
                    throw IllegalArgumentException("Conflicting search strategy flags")
                }

                useRegex -> SearchStrategy.REGEX
                useClosestMatch -> SearchStrategy.CLOSEST_MATCH
                else -> SearchStrategy.EXACT_MATCH
            }

        val results =
            runBlocking {
                findKeytoolCertificateUseCase.execute(
                    alias,
                    keystorePassword,
                    customJdkDirs,
                    searchStrategy,
                )
            }
        findCertCliPresenter.present(results, verbose, alias)
    }
}
