package edu.adarko22.jdkcerts.commands

import java.nio.file.Path

/**
 * Sealed class hierarchy representing certificate-related command actions for keytool operations.
 *
 * This hierarchy encapsulates the different types of operations that can be performed
 * on certificates using the keytool utility. Each action knows how to build the
 * appropriate command-line arguments for its specific operation.
 */
sealed class CertCommandAction {
    abstract val alias: String

    abstract val keystorePassword: String

    abstract fun buildArgs(): List<String>
}

/**
 * Action for installing a certificate into a keystore using keytool -importcert.
 *
 * This action handles the complete process of importing a certificate file into
 * a JDK truststore, including proper argument construction and keystore password handling.
 */
data class InstallCertAction(
    val certPath: Path,
    override val alias: String,
    override val keystorePassword: String,
) : CertCommandAction() {
    override fun buildArgs() =
        listOf(
            "-importcert",
            "-noprompt",
            "-trustcacerts",
            "-alias",
            alias,
            "-file",
            certPath.toString(),
            "-storepass",
            keystorePassword,
        )
}

/**
 * Action for removing a certificate from a keystore using keytool -delete.
 *
 * This action handles the removal of a certificate by its alias from a JDK
 * truststore, including proper argument construction and keystore password handling.
 */
data class RemoveCertAction(
    override val alias: String,
    override val keystorePassword: String,
) : CertCommandAction() {
    override fun buildArgs() = listOf("-delete", "-alias", alias, "-storepass", keystorePassword)
}
