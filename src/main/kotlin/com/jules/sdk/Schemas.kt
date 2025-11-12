package com.jules.sdk

import kotlinx.serialization.Serializable

@Serializable
data class Source(
    val name: String,
    val id: String,
    val createTime: String,
    val updateTime: String,
    val url: String,
    val type: String,
    val githubRepo: GithubRepoContext? = null
)

@Serializable
data class GithubRepoContext(
    val owner: String,
    val repo: String,
    val defaultBranch: String? = null,
    val startingBranch: String? = null
)

@Serializable
data class SourceContext(
    val source: String,
    val githubRepoContext: GithubRepoContext? = null
)

@Serializable
data class CreateSessionRequest(
    val prompt: String,
    val sourceContext: SourceContext? = null,
    val title: String? = null,
    val requirePlanApproval: Boolean? = null
)

@Serializable
data class Session(
    val name: String,
    val id: String,
    val createTime: String,
    val updateTime: String,
    val title: String,
    val prompt: String,
    val state: SessionState,
    val output: SessionOutput? = null
)

@Serializable
enum class SessionState {
    SESSION_STATE_UNSPECIFIED,
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

@Serializable
data class SessionOutput(
    val pullRequest: PullRequest? = null
)

@Serializable
data class PullRequest(
    val url: String
)

@Serializable
data class ListSourcesResponse(
    val sources: List<Source>? = null,
    val nextPageToken: String? = null
)

@Serializable
data class ListSessionsResponse(
    val sessions: List<Session>? = null,
    val nextPageToken: String? = null
)

@Serializable
data class ListActivitiesResponse(
    val activities: List<Activity>? = null,
    val nextPageToken: String? = null
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class Activity(
    val name: String,
    val id: String,
    val createTime: String,
    val updateTime: String,
    val description: String,
    val state: ActivityState,
    val artifacts: List<Artifact>? = null,
    val agentMessaged: AgentMessaged? = null,
    val userMessaged: UserMessaged? = null,
    val planGenerated: PlanGenerated? = null,
    val planApproved: PlanApproved? = null,
    val progressUpdated: ProgressUpdated? = null,
    val sessionCompleted: SessionCompleted? = null,
    val sessionFailed: SessionFailed? = null
)

@Serializable
enum class ActivityState {
    ACTIVITY_STATE_UNSPECIFIED,
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

@Serializable
data class AgentMessaged(
    val agentMessage: String
)

@Serializable
data class UserMessaged(
    val userMessage: String
)

@Serializable
data class PlanGenerated(
    val plan: Plan
)

@Serializable
data class Plan(
    val steps: List<PlanStep>? = null
)

@Serializable
data class PlanStep(
    val description: String
)

@Serializable
data class PlanApproved(
    val planId: String
)

@Serializable
data class ProgressUpdated(
    val title: String? = null
)

@Serializable
data class SessionCompleted(
    val outcome: String
)

@Serializable
data class SessionFailed(
    val reason: String? = null
)

@Serializable
data class Artifact(
    val name: String,
    val id: String,
    val createTime: String,
    val updateTime: String,
    val changeSet: ChangeSet? = null,
    val media: Media? = null,
    val bashOutput: BashOutput? = null
)

@Serializable
data class ChangeSet(
    val source: String,
    val patch: String
)

@Serializable
data class Media(
    val mimeType: String,
    val content: String
)

@Serializable
data class BashOutput(
    val command: String,
    val stdout: String,
    val stderr: String,
    val exitCode: Int
)

@Serializable
data class GoogleApiError(
    val code: Int,
    val message: String,
    val status: String,
    val details: List<ErrorDetail>? = null
)

@Serializable
data class ErrorDetail(
    val type: String,
    val message: String
)
