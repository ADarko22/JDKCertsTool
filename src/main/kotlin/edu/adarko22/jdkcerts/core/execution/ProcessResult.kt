package edu.adarko22.jdkcerts.core.execution

data class ProcessResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val dryRunOutput: String,
)
