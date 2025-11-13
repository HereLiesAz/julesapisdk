package com.hereliesaz.julesapisdk

class JulesSession(
    private val client: JulesClient,
    initialSession: Session
) {
    // This var holds the most recent state of the session
    var session: Session = initialSession
        private set // Can be read publicly, but only written internally

    /**
     * Sends a message to the agent in this session.
     *
     * @param prompt The prompt to send to the agent.
     * @return A `MessageResponse` object (which is typically empty).
     */
    suspend fun sendMessage(prompt: String): SdkResult<MessageResponse> {
        return client.internalSendMessage(session.name, prompt)
    }

    /**
     * Fetches the latest list of activities for this session.
     * This is the primary way to get updates after sending a message.
     *
     * @param pageSize The maximum number of activities to return.
     * @param pageToken A token for pagination.
     * @return A `ListActivitiesResponse` containing a list of activities.
     */
    suspend fun listActivities(pageSize: Int? = null, pageToken: String? = null): SdkResult<ListActivitiesResponse> {
        return client.internalListActivities(session.name, pageSize, pageToken)
    }

    /**
     * Gets a specific activity from this session.
     *
     * @param activityId The ID of the activity to retrieve.
     * @return The `Activity` object.
     */
    suspend fun getActivity(activityId: String): SdkResult<Activity> {
        return client.internalGetActivity(session.name, activityId)
    }

    /**
     * Approves the latest plan for this session.
     */
    suspend fun approvePlan(): SdkResult<Unit> {
        return client.internalApprovePlan(session.name)
    }

    /**
     * Refreshes the session's own state (e.g., to check if it has
     * moved to "COMPLETED" or "FAILED").
     *
     * @return The updated `Session` object.
     */
    suspend fun refreshSessionState(): SdkResult<Session> {
        val result = client.getSession(session.name)
        if (result is SdkResult.Success) {
            this.session = result.data // Update the internal state
        }
        return result
    }
}