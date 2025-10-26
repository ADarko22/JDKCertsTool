package edu.adarko22.process

data class ProcessResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
)

interface ProcessExecutor {
    fun runCommand(command: List<String>): ProcessResult
}

class DefaultProcessExecutor : ProcessExecutor {
    override fun runCommand(command: List<String>): ProcessResult {
        val process =
            ProcessBuilder(command)
                .redirectErrorStream(false)
                .start()

        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        return ProcessResult(stdout.trim(), stderr.trim(), exitCode)
    }
}
