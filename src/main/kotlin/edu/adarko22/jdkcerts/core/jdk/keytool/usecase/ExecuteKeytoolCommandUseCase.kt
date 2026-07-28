package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.execution.KeytoolProcessResult
import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.classifier.KeytoolErrorClassifier
import edu.adarko22.jdkcerts.core.jdk.keytool.classifier.KeytoolFailure
import edu.adarko22.jdkcerts.core.jdk.keytool.model.ExecutionContext
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult

/**
 * CQRS **command** use case: executes a mutating keytool operation across discovered JDKs.
 *
 * Discovers JDKs via [jdkDiscoverJdksUseCase], delegates execution to [KeytoolProcessRunner], then maps
 * each neutral [KeytoolProcessResult] into a semantic [KeytoolCommandResult] — using
 * [KeytoolErrorClassifier] as the single interpreter of raw keytool failures.
 */
class ExecuteKeytoolCommandUseCase(
    val jdkDiscoverJdksUseCase: DiscoverJdksUseCase,
    private val keytoolProcessRunner: KeytoolProcessRunner,
    private val errorClassifier: KeytoolErrorClassifier = KeytoolErrorClassifier(),
) {
    /**
     * Executes the given keytool command against all discovered JDKs concurrently.
     *
     * @param keytoolCommand The keytool command to run (import/delete).
     * @param executionContext The context for executing the keytool command on the system.
     * @return One [KeytoolCommandResult] per discovered JDK.
     */
    suspend fun execute(
        keytoolCommand: KeytoolCommand,
        executionContext: ExecutionContext,
    ): List<KeytoolCommandResult> {
        val jdks = jdkDiscoverJdksUseCase.discover(executionContext.customJdkPaths)
        return keytoolProcessRunner
            .runConcurrently(keytoolCommand, jdks, executionContext.masterPassword, executionContext.dryRun)
            .map { outcome ->
                when (outcome) {
                    is KeytoolProcessResult.DryRun -> {
                        KeytoolCommandResult.DryRun(outcome.jdk, outcome.previewCommand)
                    }

                    is KeytoolProcessResult.Executed -> {
                        if (outcome.exitCode == 0) {
                            KeytoolCommandResult.Success(outcome.jdk)
                        } else {
                            handleFailure(outcome, keytoolCommand.alias)
                        }
                    }
                }
            }
    }

    /**
     * Maps a non-zero keytool exit into a typed command failure, interpreting the raw output through
     * [KeytoolErrorClassifier].
     */
    private fun handleFailure(
        outcome: KeytoolProcessResult.Executed,
        alias: String,
    ): KeytoolCommandResult.Failure =
        when (val failure = errorClassifier.classify(outcome.exitCode, outcome.stdout, outcome.stderr)) {
            is KeytoolFailure.WrongPassword -> {
                KeytoolCommandResult.Failure.WrongPassword(outcome.jdk, failure.rawStderr)
            }

            is KeytoolFailure.AliasAlreadyExists -> {
                KeytoolCommandResult.Failure.AliasAlreadyExists(outcome.jdk, alias, failure.rawStderr)
            }

            is KeytoolFailure.CertificateAlreadyExists -> {
                KeytoolCommandResult.Failure.CertificateAlreadyExists(outcome.jdk, failure.conflictingAlias, failure.rawStderr)
            }

            is KeytoolFailure.AliasNotFound -> {
                KeytoolCommandResult.Failure.AliasNotFound(outcome.jdk, alias, failure.rawStderr)
            }

            is KeytoolFailure.Unknown -> {
                KeytoolCommandResult.Failure.Unknown(outcome.jdk, failure.exitCode, failure.rawStderr)
            }
        }
}
