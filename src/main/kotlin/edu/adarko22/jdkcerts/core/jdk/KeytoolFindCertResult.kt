package edu.adarko22.jdkcerts.core.jdk

/**
 * Represents the high-level result of searching for a specific certificate in a JDK.
 */
sealed class KeytoolFindCertResult {
    abstract val jdk: Jdk

    /**
     * Indicates the certificate was located and its details were successfully parsed.
     *
     * @property jdk The JDK where the certificate exists.
     * @property certificateInfo The parsed domain model containing fingerprints, dates, and owner info.
     */
    data class Found(
        override val jdk: Jdk,
        val certificateInfo: CertificateInfo,
    ) : KeytoolFindCertResult()

    /**
     * Indicates the process completed, but the specific alias or certificate could not be found.
     *
     * This may occur if the keytool command returns an "alias does not exist" error or if the
     * stdout does not contain valid certificate data.
     *
     * @property jdk The JDK searched.
     * @property reason A description of why the certificate was considered missing.
     * @property stdout The raw output from the process, useful for debugging parsing misses.
     * @property stderr The raw error output from the process.
     */
    data class NotFound(
        override val jdk: Jdk,
        val reason: String,
        val stdout: String,
        val stderr: String,
    ) : KeytoolFindCertResult()

    /**
     * Indicates a critical failure occurred during the search or parsing phase.
     *
     * @property jdk The JDK where the error occurred.
     * @property message A description of the error.
     * @property cause The underlying exception (e.g., an [IllegalArgumentException] from the parser).
     */
    data class Error(
        override val jdk: Jdk,
        val message: String,
        val cause: Throwable? = null,
    ) : KeytoolFindCertResult()
}
