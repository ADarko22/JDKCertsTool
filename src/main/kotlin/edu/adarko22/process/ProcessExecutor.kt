package edu.adarko22.process


/**
 * Interface for executing external processes with consistent error handling and result reporting.
 *
 * This abstraction allows for easy testing and different execution strategies
 * (e.g., synchronous vs asynchronous execution). It provides a clean separation
 * between process execution logic and the rest of the application.
 */
interface ProcessExecutor {
    fun runCommand(command: List<String>): ProcessResult
}

/**
 * Default implementation of ProcessExecutor using ProcessBuilder for synchronous execution.
 *
 * This implementation executes commands synchronously and captures both stdout and
 * stderr streams. It's suitable for production use and provides a foundation for
 * more sophisticated execution strategies.
 */
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

data class ProcessResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
)
