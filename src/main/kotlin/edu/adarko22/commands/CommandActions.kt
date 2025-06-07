package edu.adarko22.commands

import java.nio.file.Path

sealed class CertCommandAction {
    abstract val alias: String
    abstract val keystorePassword: String
    abstract fun buildArgs(): List<String>
}

data class InstallCertAction(
    val certPath: Path,
    override val alias: String,
    override val keystorePassword: String
) : CertCommandAction() {

    override fun buildArgs() = listOf(
        "-importcert", "-noprompt", "-trustcacerts",
        "-alias", alias, "-file", certPath.toString(), "-storepass", keystorePassword
    )
}

data class RemoveCertAction(
    override val alias: String,
    override val keystorePassword: String
) : CertCommandAction() {

    override fun buildArgs() = listOf("-delete", "-alias", alias, "-storepass", keystorePassword)
}
