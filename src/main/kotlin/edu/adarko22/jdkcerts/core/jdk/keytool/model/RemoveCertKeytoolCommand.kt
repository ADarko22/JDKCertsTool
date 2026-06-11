package edu.adarko22.jdkcerts.core.jdk.keytool.model

/**
 * Represents a `keytool -delete` command for removing certificates.
 *
 * @property alias The certificate alias.
 * @property keystorePassword The keystore password.
 */
data class RemoveCertKeytoolCommand(
    override val alias: String,
    override val keystorePassword: String,
) : KeytoolCommand {
    override fun getArgs(): List<String> =
        listOf(
            "-delete",
            "-alias",
            alias,
            "-storepass",
            keystorePassword,
        )
}
