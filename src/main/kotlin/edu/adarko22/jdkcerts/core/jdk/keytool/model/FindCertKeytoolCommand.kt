package edu.adarko22.jdkcerts.core.jdk.keytool.model

/**
 * Represents a `keytool -list` command for finding certificates.
 *
 * @property alias The certificate alias.
 * @property keystorePassword The keystore password.
 * @property searchStrategy The search strategy (EXACT_MATCH, CLOSEST_MATCH, REGEX).
 */
data class FindCertKeytoolCommand(
    override val alias: String,
    override val keystorePassword: String,
    val searchStrategy: SearchStrategy,
) : KeytoolCommand {
    override fun getArgs(): List<String> =
        if (searchStrategy == SearchStrategy.EXACT_MATCH) {
            listOf(
                "-list",
                "-v",
                "-alias",
                alias,
                "-storepass",
                keystorePassword,
            )
        } else {
            listOf(
                "-list",
                "-v",
                "-storepass",
                keystorePassword,
            )
        }
}

enum class SearchStrategy {
    EXACT_MATCH,
    CLOSEST_MATCH,
    REGEX,
}
