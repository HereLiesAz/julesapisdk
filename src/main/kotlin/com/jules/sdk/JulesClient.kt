package com.jules.sdk

class JulesClient(
    apiKey: String,
    baseUrl: String = "https://jules.googleapis.com/v1alpha",
    retryConfig: RetryConfig = RetryConfig()
) {
    private val httpClient = HttpClient(apiKey, baseUrl, retryConfig = retryConfig)

    suspend fun listSources(pageSize: Int? = null, pageToken: String? = null, filter: String? = null): ListSourcesResponse {
        val params = mutableMapOf<String, String>()
        pageSize?.let { params["pageSize"] = it.toString() }
        pageToken?.let { params["pageToken"] = it }
        filter?.let { params["filter"] = it }
        return httpClient.get<ListSourcesResponse>("/sources", params)
    }

    suspend fun getSource(sourceId: String): Source {
        return httpClient.get<Source>("/sources/$sourceId")
    }

    suspend fun createSession(request: CreateSessionRequest): Session {
        return httpClient.postAndReceive<Session>("/sessions", request)
    }

    suspend fun listSessions(pageSize: Int? = null, pageToken: String? = null): ListSessionsResponse {
        val params = mutableMapOf<String, String>()
        pageSize?.let { params["pageSize"] = it.toString() }
        pageToken?.let { params["pageToken"] = it }
        return httpClient.get<ListSessionsResponse>("/sessions", params)
    }

    suspend fun getSession(sessionId: String): Session {
        return httpClient.get<Session>("/sessions/$sessionId")
    }

    suspend fun approvePlan(sessionId: String) {
        httpClient.post("/sessions/$sessionId:approvePlan")
    }

    suspend fun listActivities(sessionId: String, pageSize: Int? = null, pageToken: String? = null): ListActivitiesResponse {
        val params = mutableMapOf<String, String>()
        pageSize?.let { params["pageSize"] = it.toString() }
        pageToken?.let { params["pageToken"] = it }
        return httpClient.get<ListActivitiesResponse>("/sessions/$sessionId/activities", params)
    }

    suspend fun getActivity(sessionId: String, activityId: String): Activity {
        return httpClient.get<Activity>("/sessions/$sessionId/activities/$activityId")
    }

    suspend fun sendMessage(sessionId: String, prompt: String) {
        require(prompt.isNotBlank()) { "Prompt must be a non-empty string" }
        httpClient.postWithBody("/sessions/$sessionId:sendMessage", mapOf("prompt" to prompt))
    }
}
