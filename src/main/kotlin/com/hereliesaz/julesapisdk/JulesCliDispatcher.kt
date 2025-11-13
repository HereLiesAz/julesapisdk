package com.hereliesaz.julesapisdk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * A simple data class representing the result of a CLI command.
 */
data class CliTaskReceipt(
    val command: String,
    val stdOut: String,
    val stdErr: String,
    val exitCode: Int
)

/**
 * Client for the asynchronous, "fire-and-forget" Jules CLI tool.
 *
 * This class builds and executes shell commands for `jules-tools`.
 * It is designed for scripting and task dispatch, not for stateful
 * interaction.
 */
class JulesCliDispatcher(
    private val config: CliConfig
) {

    /**
     * Dispatches a new task using `jules remote new`.
     *
     * This is a "fire-and-forget" operation. It returns a receipt with
     * the command's STDOUT/STDERR, not a Session object.
     *
     * @param repo The repository to run against (e.g., "my-org/my-repo" or ".")
     * @param prompt The prompt for the task (e.g., "fix the bug in login.kt")
     * @return A SdkResult containing a CliTaskReceipt.
     */
    suspend fun createTask(repo: String, prompt: String): SdkResult<CliTaskReceipt> {
        val command = listOf(
            config.cliPath,
            "remote",
            "new",
            "--repo", repo,
            "--session", prompt
        )
        return executeCliCommand(command)
    }

    /**
     * Executes a given shell command and captures its output.
     */
    private suspend fun executeCliCommand(command: List<String>): SdkResult<CliTaskReceipt> {
        return withContext(Dispatchers.IO) { // Run process off the main thread
            try {
                val process = ProcessBuilder(command)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()

                // Note: You may want a more robust, non-blocking reader here
                val stdOut = process.inputStream.bufferedReader().readText()
                val stdErr = process.errorStream.bufferedReader().readText()

                val exited = process.waitFor(30, TimeUnit.SECONDS)
                val exitCode = if (exited) process.exitValue() else -1

                val receipt = CliTaskReceipt(command.joinToString(" "), stdOut, stdErr, exitCode)

                if (exitCode != 0) {
                    SdkResult.Error(exitCode, "CLI command failed:\n$stdErr")
                } else {
                    SdkResult.Success(receipt)
                }
            } catch (e: Exception) {
                SdkResult.NetworkError(e) // Use NetworkError to represent IO/Process exceptions
            }
        }
    }
}