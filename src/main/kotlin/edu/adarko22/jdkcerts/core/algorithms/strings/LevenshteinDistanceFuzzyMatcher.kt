package edu.adarko22.jdkcerts.core.algorithms.strings

import kotlin.math.max

/**
 * A token-aware, Levenshtein-distance based fuzzy matcher.
 *
 * Computes the normalized Levenshtein distance between two character sequences and
 * returns a similarity score in the range `0.0..1.0`, where `1.0` is an exact match.
 *
 * **Token Matching:**
 * To support domain-specific naming conventions (e.g., `my-cert-alias`), the target text
 * is split by common delimiters (`-`, `_`, `.`, `:`). The matcher evaluates the search key
 * against the full text AND each individual token, returning the highest score.
 *
 * **Special cases:**
 * - Case-insensitive: All inputs are normalized to lowercase before comparison.
 * - Empty inputs: Throws an [IllegalArgumentException] to prevent meaningless comparisons.
 */
class LevenshteinDistanceFuzzyMatcher : FuzzyMatcher {
    override fun similarityScore(
        text: String,
        key: String,
    ): Double {
        if (text.isEmpty() || key.isEmpty()) throw IllegalArgumentException()

        val normalizedText = text.lowercase()
        val normalizedKey = key.lowercase()

        val tokens = normalizedText.split('-', '_', '.', ':').filter(String::isNotEmpty)

        val bestTokenScore =
            tokens.maxOfOrNull {
                score(editDistance(it, normalizedKey), it.length, normalizedKey.length)
            } ?: 0.0

        val fullTextScore = score(editDistance(normalizedText, normalizedKey), normalizedText.length, normalizedKey.length)

        return maxOf(bestTokenScore, fullTextScore)
    }

    private fun score(
        editDistance: Int,
        textLen: Int,
        keyLen: Int,
    ): Double {
        val maxLen = max(textLen, keyLen).toDouble()
        return (maxLen - editDistance) / maxLen
    }

    /**
     * Space-optimized Levenshtein Distance.
     * Uses two 1D primitive arrays instead of a full 2D object matrix to minimize GC overhead.
     */
    private fun editDistance(
        text: CharSequence,
        key: CharSequence,
    ): Int {
        var v0 = IntArray(key.length + 1) { it }
        var v1 = IntArray(key.length + 1)

        for (i in text.indices) {
            v1[0] = i + 1
            for (j in key.indices) {
                val cost = if (text[i] == key[j]) 0 else 1
                v1[j + 1] =
                    minOf(
                        // deletion
                        v1[j] + 1,
                        // insertion
                        v0[j + 1] + 1,
                        // substitution
                        v0[j] + cost,
                    )
            }
            // Swap arrays for the next row
            val temp = v0
            v0 = v1
            v1 = temp
        }

        return v0[key.length]
    }
}
