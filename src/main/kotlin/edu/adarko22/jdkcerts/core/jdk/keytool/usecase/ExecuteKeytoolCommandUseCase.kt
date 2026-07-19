package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.execution.KeytoolProcessResult
import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.Jdk
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
            .map { outcome -> outcome.toCommandResult(keytoolCommand.alias) }
    }

    private fun KeytoolProcessResult.toCommandResult(alias: String): KeytoolCommandResult =
        when (this) {
            is KeytoolProcessResult.DryRun -> {
                KeytoolCommandResult.DryRun(jdk, previewCommand)
            }

            is KeytoolProcessResult.Executed -> {
                if (exitCode == 0) {
                    KeytoolCommandResult.Success(jdk)
                } else {
                    errorClassifier.classify(exitCode, stdout, stderr).toCommandFailure(jdk, alias)
                }
            }
        }

    private fun KeytoolFailure.toCommandFailure(
        jdk: Jdk,
        alias: String,
    ): KeytoolCommandResult.Failure =
        when (this) {
            is KeytoolFailure.WrongPassword -> {
                KeytoolCommandResult.Failure.WrongPassword(jdk, rawStderr)
            }

            is KeytoolFailure.AliasAlreadyExists -> {
                KeytoolCommandResult.Failure.AliasAlreadyExists(jdk, alias, rawStderr)
            }

            is KeytoolFailure.CertificateAlreadyExists -> {
                KeytoolCommandResult.Failure.CertificateAlreadyExists(jdk, conflictingAlias, rawStderr)
            }

            is KeytoolFailure.AliasNotFound -> {
                KeytoolCommandResult.Failure.AliasNotFound(jdk, alias, rawStderr)
            }

            is KeytoolFailure.Unknown -> {
                KeytoolCommandResult.Failure.Unknown(jdk, exitCode, rawStderr)
            }
        }
}
