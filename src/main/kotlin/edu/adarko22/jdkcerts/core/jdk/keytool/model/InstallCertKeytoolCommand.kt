package edu.adarko22.jdkcerts.core.jdk.keytool.model

/**
 * Represents a `keytool -importcert` command for installing certificates.
 *
 * @property alias The certificate alias.
 * @property keystorePassword The keystore password.
 * @property certificateAbsolutePath The absolute path to the certificate file.
 */
data class InstallCertKeytoolCommand(
    override val alias: String,
    override val keystorePassword: String,
    val certificateAbsolutePath: String,
) : KeytoolCommand {
    override fun getArgs(): List<String> =
        listOf(
            "-importcert",
            "-noprompt",
            "-trustcacerts",
            "-alias",
            alias,
            "-file",
            certificateAbsolutePath,
            "-storepass",
            keystorePassword,
        )
}
