package edu.adarko22.jdkcerts.core.algorithms.strings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LevenshteinDistanceFuzzyMatcherTest {
    private val matcher = LevenshteinDistanceFuzzyMatcher()

    @Test
    fun `exact match returns score 1`() {
        val score = matcher.similarityScore("my-cert-alias", "my-cert-alias")
        assertEquals(1.0, score, 0.0001)
    }

    @Test
    fun `empty strings score 1`() {
        val score = matcher.similarityScore("", "")
        assertEquals(1.0, score, 0.0001)
    }

    @Test
    fun `empty vs non-empty score 0`() {
        val score = matcher.similarityScore("", "nonempty")
        assertEquals(0.0, score, 0.0001)
    }

    @Test
    fun `very different strings yield low score`() {
        val score = matcher.similarityScore("abcdef", "uvwxyz")
        assertEquals(0.0, score, 0.0001)
    }

    @Test
    fun `small typo yields high score`() {
        val score = matcher.similarityScore("certificate-alias", "certificat alis")
        assertEquals(0.82, score, 0.01)
    }
}
