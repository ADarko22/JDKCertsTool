package edu.adarko22.jdkcerts.core.jdk.keytool.model

/**
 * Represents a `keytool -delete` command for removing certificates.
 *
 * @property alias The certificate alias.
 */
data class RemoveCertKeytoolCommand(
    override val alias: String,
) : KeytoolCommand {
    override fun getArgs(): List<String> =
        listOf(
            "-delete",
            "-alias",
            alias,
        )
}
