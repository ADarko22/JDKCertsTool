package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandFactory
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult
import java.nio.file.Path

/**
 * Use case for installing a certificate into JDK keystores across all discovered JDK installations.
 *
 * This use case encapsulates the certificate installation workflow by delegating to
 * [ExecuteKeytoolCommandUseCase] to execute the keytool import-cert command with the provided parameters.
 *
 * @param executeKeytoolCommandUseCase Component responsible for executing keytool commands.
 */
class InstallKeytoolCertificateUseCase(
    private val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
) {
    /**
     * Installs a certificate from the specified file into all discovered JDK keystores.
     *
     * @param alias The alias under which the certificate will be stored in the keystore.
     * @param keystorePassword The password to the keystore.
     * @param certificatePath The file path to the certificate to be installed.
     * @param customJdkDirs Optional custom JDK directories to include in discovery.
     * @param dryRun If true, the installation is simulated without making changes.
     * @return List of [KeytoolCommandResult] objects representing the outcome for each JDK.
     */
    fun execute(
        alias: String,
        keystorePassword: String,
        certificatePath: Path,
        customJdkDirs: List<Path>,
        dryRun: Boolean,
    ): List<KeytoolCommandResult> {
        val command =
            KeytoolCommandFactory
                .installCertificateKeytoolCommand(
                    alias,
                    keystorePassword,
                    certificatePath.toAbsolutePath().toString(),
                )
        return executeKeytoolCommandUseCase.execute(command, customJdkDirs, dryRun)
    }
}
