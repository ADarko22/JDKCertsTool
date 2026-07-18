package edu.adarko22.jdkcerts.core.jdk.keytool.model

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
