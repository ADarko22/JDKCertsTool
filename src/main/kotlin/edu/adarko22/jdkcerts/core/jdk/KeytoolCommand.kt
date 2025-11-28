package edu.adarko22.jdkcerts.core.jdk

/**
 * Represents a `keytool` command with arguments and optional keystore resolution.
 *
 * @property args Command-line arguments for `keytool`.
 * @property resolveKeystore Whether to automatically resolve the keystore path.
 */
data class KeytoolCommand(
    val args: List<String>,
    val resolveKeystore: Boolean = false,
) {
    /**
     * Builder for [KeytoolCommand] to simplify command construction.
     */
    class Builder(
        private val args: MutableList<String> = mutableListOf(),
        private var resolveKeystore: Boolean = false,
    ) {
        /** Adds a command argument. */
        fun addArg(arg: String): Builder {
            args.add(arg)
            return this
        }

        /** Enables or disables automatic keystore resolution. */
        fun withKeystoreResolution(enable: Boolean = true): Builder {
            resolveKeystore = enable
            return this
        }

        /** Builds the [KeytoolCommand] instance. */
        fun build(): KeytoolCommand = KeytoolCommand(args.toList(), resolveKeystore)
    }
}
