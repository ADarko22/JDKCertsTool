package edu.adarko22.jdkcerts.core.execution

import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperation
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperationResult

interface KeytoolProcessRunner {
    suspend fun runConcurrently(
        operation: KeytoolOperation,
        jdks: List<Jdk>,
        masterPassword: String,
        dryRun: Boolean,
    ): List<KeytoolOperationResult>
}
