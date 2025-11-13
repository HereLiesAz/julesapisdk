package com.hereliesaz.julesapisdk

import kotlinx.serialization.Serializable

/**
 * Represents an active, stateful session with the Jules API.
 *
 * This class holds the state of a specific session and provides methods
 * for interacting with it, such as sending messages and listing activities.
 *
 * @property client The JulesApiClient instance used to create this session.
 * @property session The underlying Session data object.
 */
data class JulesSession(
    // *** MODIFIED: Renamed to use JulesApiClient ***
    internal val client: JulesApiClient,
    val session: Session
) {
    private val sessionId = session.name

    /**
     * Send a message to the agent in this session.
     * This is asynchronous; call listActivities() to see the response.
     */
    suspend fun sendMessage(prompt: String): SdkResult<MessageResponse> {
        val request = SendMessageRequest(prompt)
        return client.httpClient.post("$sessionId:sendMessage", request)
    }

    /**
     * List activities for this session.
     * Call this after sendMessage to get new messages.
     */
    suspend fun listActivities(
        pageSize: Int? = null,
        pageToken: String? = null
    ): SdkResult<ListActivitiesResponse> {
        val params = buildMap {
            pageSize?.let { put("pageSize", it.toString()) }
            pageToken?.let { put("pageToken", it) }
        }
        return client.httpClient.get("$sessionId/activities", params)
    }

    /**
     * Get a specific activity for this session.
     */
    suspend fun getActivity(activityId: String): SdkResult<Activity> {
        return client.httpClient.get("$sessionId/activities/$activityId")
    }

    /**
     * Approve the latest plan for this session.
     */
    suspend fun approvePlan(): SdkResult<Unit> {
        // Explicitly provided generic types for Request (Any?) and Response (Unit)
        return client.httpClient.post<Any?, Unit>("$sessionId:approvePlan")
    }

    /**
     * Fetches the latest state for this session object.
     */
    suspend fun refreshSessionState(): SdkResult<Session> {
        return client.httpClient.get(sessionId)
    }
}