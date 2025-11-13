package com.hereliesaz.julesapisdk

/**
 * The main facade for the Jules SDK.
 *
 * This class provides unified access to the two different paradigms
 * for interacting with Jules:
 *
 * 1.  `api`: The synchronous, stateful v1alpha REST API client.
 * Used for interactive applications (like the test app) that need
 * to list sessions, activities, and get immediate feedback.
 *
 * 2.  `cli`: The asynchronous, "fire-and-forget" CLI dispatcher.
 * Used for scripting, CI/CD, or backend processes to dispatch
 * tasks without waiting for a response object.
 */
class Jules(
    apiConfig: ApiConfig,
    cliConfig: CliConfig = CliConfig()
) {

    /**
     * The stateful, request-response REST API client.
     */
    val api: JulesApiClient = JulesApiClient(apiConfig) // This line (26) requires the new constructor

    /**
     * The asynchronous "fire-and-forget" CLI task dispatcher.
     */
    val cli: JulesCliDispatcher = JulesCliDispatcher(cliConfig)
}

/**
 * Configuration for the REST API client.
 */
data class ApiConfig(
    val apiKey: String,
    val apiVersion: String = "v1alpha",
    val baseUrl: String = "https://jules.googleapis.com",
    // *** MODIFIED: Enable retries by default per the audit (was maxRetries = 0) ***
    val retryConfig: RetryConfig = RetryConfig(maxRetries = 3)
)

/**
 * Configuration for the CLI dispatcher.
 */
data class CliConfig(
    /**
     * The path to the 'jules' executable. Assumes it's on the system PATH
     * by default.
     */
    val cliPath: String = "jules"
)