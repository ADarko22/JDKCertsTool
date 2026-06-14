package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.RemoveCertKeytoolCommand
import java.nio.file.Path

/**
 * Use case for removing a certificate from JDK keystores across all discovered JDK installations.
 *
 * This use case encapsulates the certificate removal workflow by delegating to
 * [ExecuteKeytoolCommandUseCase] to execute the keytool delete command with the provided parameters.
 *
 * @param executeKeytoolCommandUseCase Component responsible for executing keytool commands.
 */
class RemoveKeytoolCertificateUseCase(
    private val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
) {
    /**
     * Removes a certificate by its alias from all discovered JDK keystores.
     *
     * @param alias The alias of the certificate to be removed from the keystore.
     * @param keystorePassword The password to the keystore.
     * @param customJdkDirs Optional custom JDK directories to include in discovery.
     * @param dryRun If true, the removal is simulated without making changes.
     * @return List of [KeytoolCommandResult] objects representing the outcome for each JDK.
     */
    suspend fun execute(
        alias: String,
        keystorePassword: String,
        customJdkDirs: List<Path>,
        dryRun: Boolean,
    ): List<KeytoolCommandResult> {
        val command = RemoveCertKeytoolCommand(alias, keystorePassword)
        return executeKeytoolCommandUseCase.execute(command, customJdkDirs, dryRun)
    }
}
