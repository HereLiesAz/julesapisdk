package com.jules.sdk

import kotlinx.serialization.Serializable

@Serializable
data class GithubRepoContext(
    val startingBranch: String
)

@Serializable
data class SourceContext(
    val source: String,
    val githubRepoContext: GithubRepoContext? = null
)

@Serializable
enum class AutomationMode {
    AUTOMATION_MODE_UNSPECIFIED,
    AUTO_CREATE_PR
}

@Serializable
enum class SessionState {
    STATE_UNSPECIFIED,
    QUEUED,
    PLANNING,
    AWAITING_PLAN_APPROVAL,
    AWAITING_USER_FEEDBACK,
    IN_PROGRESS,
    PAUSED,
    FAILED,
    COMPLETED
}

@Serializable
data class CreateSessionRequest(
    val prompt: String,
    val sourceContext: SourceContext,
    val title: String? = null,
    val requirePlanApproval: Boolean? = null,
    val automationMode: AutomationMode? = null
)

@Serializable
data class PullRequest(
    val url: String,
    val title: String,
    val description: String
)

@Serializable
data class SessionOutput(
    val pullRequest: PullRequest? = null
)

@Serializable
data class Session(
    val name: String,
    val id: String,
    val createTime: String,
    val updateTime: String,
    val state: SessionState,
    val url: String,
    val prompt: String,
    val sourceContext: SourceContext,
    val title: String? = null,
    val requirePlanApproval: Boolean? = null,
    val automationMode: AutomationMode? = null,
    val outputs: List<SessionOutput>? = null
)

@Serializable
data class ListSessionsResponse(
    val sessions: List<Session>? = null,
    val nextPageToken: String? = null
)

@Serializable
data class Artifact(
    val type: String,
    val content: String
)

@Serializable
data class Activity(
    val id: String,
    val createTime: String,
    val updateTime: String,
    val prompt: String,
    val state: String,
    val artifacts: List<Artifact>? = null
)

@Serializable
data class ListActivitiesResponse(
    val activities: List<Activity>? = null,
    val nextPageToken: String? = null
)

@Serializable
data class Source(
    val name: String,
    val id: String,
    val createTime: String,
    val updateTime: String,
    val url: String,
    val type: String
)

@Serializable
data class ListSourcesResponse(
    val sources: List<Source>? = null,
    val nextPageToken: String? = null
)

@Serializable
data class ErrorDetail(
    val type: String,
    val message: String
)

@Serializable
data class GoogleApiError(
    val code: Int,
    val message: String,
    val status: String,
    val details: List<ErrorDetail>? = null
)
