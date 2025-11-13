package com.hereliesaz.julesapisdk

import kotlinx.serialization.json.Json

/**
 * Client for the stateful, synchronous Jules v1alpha REST API.
 * This class is a factory for creating and retrieving sessions.
 *
 * (This class was formerly JulesClient)
 *
 * @param config Configuration for the REST API.
 */
class JulesApiClient(
    // *** MODIFIED: This constructor now accepts ApiConfig ***
    // This fixes the "Argument type mismatch" compiler error.
    private val config: ApiConfig
) {
    // Internal HTTP client wrapper
    internal val httpClient = JulesHttpClient(
        apiKey = config.apiKey,
        apiVersion = config.apiVersion,
        baseUrl = config.baseUrl,
        retryConfig = config.retryConfig
    )

    // JSON parser for manual operations (if needed)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * List all available sources (e.g., GitHub repos).
     */
    suspend fun listSources(
        pageSize: Int? = null,
        pageToken: String? = null,
        filter: String? = null
    ): SdkResult<ListSourcesResponse> {
        val params = buildMap {
            pageSize?.let { put("pageSize", it.toString()) }
            pageToken?.let { put("pageToken", it) }
            filter?.let { put("filter", it) }
        }
        return httpClient.get("sources", params)
    }

    /**
     * Get details of a specific source.
     */
    suspend fun getSource(sourceId: String): SdkResult<GithubRepoSource> {
        return httpClient.get("sources/$sourceId")
    }

    /**
     * Create a new session.
     * On success, returns a stateful JulesSession object.
     */
    suspend fun createSession(request: CreateSessionRequest): SdkResult<JulesSession> {
        return when (val result = httpClient.post<CreateSessionRequest, Session>("sessions", request)) {
            is SdkResult.Success -> {
                // Wrap the returned Session in our stateful JulesSession
                SdkResult.Success(JulesSession(this, result.data))
            }
            is SdkResult.Error -> SdkResult.Error(result.code, result.body)
            is SdkResult.NetworkError -> SdkResult.NetworkError(result.throwable)
        }
    }

    /**
     * List all sessions.
     */
    suspend fun listSessions(
        pageSize: Int? = null,
        pageToken: String? = null
    ): SdkResult<ListSessionsResponse> {
        val params = buildMap {
            pageSize?.let { put("pageSize", it.toString()) }
            pageToken?.let { put("pageToken", it) }
        }
        return httpClient.get("sessions", params)
    }

    /**
     * Get details of a specific session.
     * This is the correct way to resume a session, as listSessions() returns partial objects.
     */
    suspend fun getSession(sessionId: String): SdkResult<JulesSession> {
        return when (val result = httpClient.get<Session>(sessionId)) {
            is SdkResult.Success -> {
                // Manually wrap the successful Session result in our stateful JulesSession
                SdkResult.Success(JulesSession(this, result.data))
            }
            is SdkResult.Error -> SdkResult.Error(result.code, result.body)
            is SdkResult.NetworkError -> SdkResult.NetworkError(result.throwable)
        }
    }
}