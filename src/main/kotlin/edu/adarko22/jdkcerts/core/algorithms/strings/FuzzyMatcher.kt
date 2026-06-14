package edu.adarko22.jdkcerts.core.algorithms.strings

/**
 * Contract for fuzzy string matching algorithms.
 *
 * Implementations compute a similarity score between two character sequences.
 * The score is a Double in the range 0.0..1.0 where 0.0 indicates no similarity
 * and 1.0 indicates a perfect match.
 *
 * @see LevenshteinDistanceFuzzyMatcher
 */
interface FuzzyMatcher {
    /**
     * Evaluates how closely the search [key] matches the target [text].
     *
     * Note that implementations are not required to be symmetrical; `similarityScore(A, B)`
     * does not necessarily equal `similarityScore(B, A)` if the algorithm treats the search
     * query differently than the target text (e.g., token-based subset matching).
     *
     * @param text The target string being evaluated (e.g., an existing certificate alias).
     * @param key The search query provided by the user.
     * @return A normalized similarity score from `0.0` to `1.0`.
     * @throws IllegalArgumentException If either [text] or [key] is empty.
     */
    fun similarityScore(
        text: String,
        key: String,
    ): Double
}
