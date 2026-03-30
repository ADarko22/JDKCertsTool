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
     * Compute a similarity score between two character sequences.
     *
     * @param s1 first input sequence
     * @param s2 second input sequence
     * @return similarity score between 0.0 (no match) and 1.0 (exact match)
     */
    fun similarityScore(
        s1: CharSequence,
        s2: CharSequence,
    ): Double
}
