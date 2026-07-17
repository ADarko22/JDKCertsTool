package edu.adarko22.jdkcerts.core.jdk.keytool.model

/**
 * Represents a `keytool -list` command for finding certificates.
 *
 * The command adapts based on the search strategy:
 * - **EXACT_MATCH**: Uses keytool's native `-alias` option for fast, precise lookup.
 *   Delegates to keytool internals. Fastest method.
 * - **REGEX**: Retrieves all certificates with `-list -v` and applies regex pattern matching.
 *   Requires post-processing of all keystore entries.
 * - **CLOSEST_MATCH**: Retrieves all certificates and applies edit/vector distance algorithm.
 *   Requires post-processing of all keystore entries.
 *
 * @property alias The certificate alias or search pattern.
 * @property searchStrategy The search strategy (EXACT_MATCH, CLOSEST_MATCH, REGEX).
 */
data class FindCertKeytoolQuery(
    override val alias: String,
    val searchStrategy: SearchStrategy,
) : KeytoolQuery {
    override fun getArgs(): List<String> =
        if (searchStrategy == SearchStrategy.EXACT_MATCH) {
            listOf(
                "-list",
                "-v",
                "-alias",
                alias,
            )
        } else {
            listOf(
                "-list",
                "-v",
            )
        }
}

/**
 * Search strategy for certificate discovery in keystores.
 *
 * - **EXACT_MATCH**: Direct alias lookup using keytool's `-alias` option.
 *   - Fastest method
 *   - Leverages keytool internals
 *   - Best for known aliases
 *   - Returns single certificate or not found
 *
 * - **REGEX**: Pattern matching against all keystore entries.
 *   - Medium performance
 *   - Requires loading all certificates
 *   - Best for pattern-based searches
 *   - May return multiple matches
 *
 * - **CLOSEST_MATCH**: Edit/vector distance matching against all entries.
 *   - Slowest method (full keystore scan required)
 *   - Requires distance calculation for all entries
 *   - Best for typos/fuzzy matching
 *   - Returns best single match
 */
enum class SearchStrategy {
    /**
     * Direct alias lookup (default). Fast, uses keytool's native `-alias` option.
     */
    EXACT_MATCH,

    /**
     * Fuzzy matching using edit/vector distance.
     */
    CLOSEST_MATCH,

    /**
     * Regex pattern matching across all keystore entries.
     */
    REGEX,
}
