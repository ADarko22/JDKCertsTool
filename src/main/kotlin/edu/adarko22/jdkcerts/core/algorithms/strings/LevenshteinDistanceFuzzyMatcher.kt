package edu.adarko22.jdkcerts.core.algorithms.strings

import kotlin.math.max

/**
 * Levenshtein-distance based fuzzy matcher.
 *
 * Computes the normalized Levenshtein distance between two character sequences and
 * returns a similarity score in the range 0.0..1.0 where 1.0 is an exact match.
 *
 * The normalized score is computed as:
 *   1.0 - (levenshteinDistance(s1, s2) / max(|s1|, |s2|))
 *
 * Special cases:
 * - Both inputs empty -> returns 1.0
 * - One input empty -> returns 0.0
 */
class LevenshteinDistanceFuzzyMatcher : FuzzyMatcher {
    override fun similarityScore(
        s1: CharSequence,
        s2: CharSequence,
    ): Double {
        val s1Length = s1.length
        val s2Length = s2.length

        if (s1Length == 0 && s2Length == 0) return 1.0

        if (s1Length == 0 || s2Length == 0) return 0.0

        val d = Array(s1Length + 1) { Array(s2Length + 1) { 0 } }

        for (i in 1..s1Length) {
            d[i][0] = i
        }

        for (j in 1..s2Length) {
            d[0][j] = j
        }

        for (i in 1..s1Length) {
            for (j in 1..s2Length) {
                val editCost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                d[i][j] =
                    minOf(
                        // deletion
                        d[i - 1][j] + 1,
                        // insert
                        d[i][j - 1] + 1,
                        // substitution
                        d[i - 1][j - 1] + editCost,
                    )
            }
        }

        return 1.0 - d[s1Length][s2Length].toDouble() / max(s1Length, s2Length)
    }
}
