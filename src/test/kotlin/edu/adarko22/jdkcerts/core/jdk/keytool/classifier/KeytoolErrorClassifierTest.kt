package edu.adarko22.jdkcerts.core.jdk.keytool.classifier

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class KeytoolErrorClassifierTest {
    private val classifier = KeytoolErrorClassifier()

    @Test
    fun `classifies incorrect password`() {
        val failure = classifier.classify(1, "", "keystore password was incorrect")

        assertInstanceOf(KeytoolFailure.WrongPassword::class.java, failure)
    }

    @Test
    fun `classifies tampered keystore as wrong password`() {
        val failure = classifier.classify(1, "", "keystore was tampered with, or password was incorrect")

        assertInstanceOf(KeytoolFailure.WrongPassword::class.java, failure)
    }

    @Test
    fun `classifies certificate already present under another alias and extracts it`() {
        val failure =
            classifier.classify(1, "Certificate already exists in keystore under alias <existing-ca>", "")

        val certExists = assertInstanceOf(KeytoolFailure.CertificateAlreadyExists::class.java, failure)
        assertEquals("existing-ca", certExists.conflictingAlias)
    }

    @Test
    fun `classifies taken alias`() {
        val failure = classifier.classify(1, "", "Certificate not imported, alias <my-cert> already exists")

        assertInstanceOf(KeytoolFailure.AliasAlreadyExists::class.java, failure)
    }

    @Test
    fun `classifies missing alias`() {
        val failure = classifier.classify(1, "", "keytool error: Alias <my-cert> does not exist")

        assertInstanceOf(KeytoolFailure.AliasNotFound::class.java, failure)
    }

    @Test
    fun `falls back to Unknown carrying the exit code`() {
        val failure = classifier.classify(3, "", "some unexpected failure")

        val unknown = assertInstanceOf(KeytoolFailure.Unknown::class.java, failure)
        assertEquals(3, unknown.exitCode)
    }
}
