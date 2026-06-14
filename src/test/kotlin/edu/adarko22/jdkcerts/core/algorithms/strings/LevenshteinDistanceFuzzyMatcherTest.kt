package edu.adarko22.jdkcerts.core.algorithms.strings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LevenshteinDistanceFuzzyMatcherTest {
    private val matcher = LevenshteinDistanceFuzzyMatcher()

    class LevenshteinDistanceFuzzyMatcherTest {
        private val matcher = LevenshteinDistanceFuzzyMatcher()

        @Test
        fun `exact match returns score 1`() {
            val score = matcher.similarityScore("my-cert-alias", "my-cert-alias")
            assertEquals(1.0, score, 0.0001)
        }

        @Test
        fun `empty strings throw exception`() {
            assertThrows<IllegalArgumentException> { matcher.similarityScore("", "") }
            assertThrows<IllegalArgumentException> { matcher.similarityScore("my-cert-alias", "") }
            assertThrows<IllegalArgumentException> { matcher.similarityScore("", "alias") }
        }

        @Test
        fun `very different strings yield low score`() {
            val score = matcher.similarityScore("abcdef", "uvwxyz")
            assertEquals(0.0, score, 0.0001)
        }

        @Test
        fun `small typo yields high score`() {
            // "certificat alis" vs "certificate-alias" -> 3 edits over 17 chars
            val score = matcher.similarityScore("certificate-alias", "certificat alis")
            assertEquals(0.82, score, 0.01)
        }

        @Test
        fun `is completely case insensitive`() {
            val score = matcher.similarityScore("MY-CERT-ALIAS", "my-cert-alias")
            assertEquals(1.0, score, 0.0001)
        }

        @Test
        fun `exact token match yields score 1`() {
            // The key "alias" exactly matches the 3rd token of the text
            val score = matcher.similarityScore("my-cert-alias", "alias")
            assertEquals(1.0, score, 0.0001)
        }

        @Test
        fun `partial token match yields high score`() {
            // The key "tomct" is a 1-character typo of the token "tomcat" -> score 5/6 = ~0.833
            val score = matcher.similarityScore("tomcat-server-cert", "tomct")
            assertEquals(0.8333, score, 0.001)
        }

        @Test
        fun `delimiters are handled correctly for token extraction`() {
            // Matches the "server" token exactly, testing all delimiters
            assertEquals(1.0, matcher.similarityScore("node_server", "server"), 0.0001)
            assertEquals(1.0, matcher.similarityScore("app.server", "server"), 0.0001)
            assertEquals(1.0, matcher.similarityScore("host:server", "server"), 0.0001)
        }
    }
}
