package com.hereliesaz.julesapisdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// -----------------------------------------------------------------
// Sources
// -----------------------------------------------------------------

@Serializable
data class ListSourcesResponse(
    val sources: List<GithubRepoSource>? = null,
    val nextPageToken: String? = null
)

@Serializable
data class GithubRepo(
    val owner: String,
    val repo: String,
    // Per original context, this API field is unreliable
    val isPrivate: Boolean? = null,
    val defaultBranch: GithubBranch? = null,
    val branches: List<GithubBranch>? = null
)

@Serializable
data class GithubBranch(
    val displayName: String
)

@Serializable
sealed class Source {
    abstract val name: String
    abstract val id: String
}

@Serializable
@SerialName("githubRepo")
data class GithubRepoSource(
    override val name: String,
    override val id: String,
    val githubRepo: GithubRepo
) : Source()

@Serializable
data class SourceContext(
    val source: String,
    val githubRepoContext: GithubRepoContext? = null
)

@Serializable
data class GithubRepoContext(
    val startingBranch: String
)

// -----------------------------------------------------------------
// Sessions
// -----------------------------------------------------------------

@Serializable
data class CreateSessionRequest(
    val prompt: String,
    val sourceContext: SourceContext,
    val title: String? = null,
    val requirePlanApproval: Boolean? = null,
    val automationMode: String? = null // e.g., "AUTO_CREATE_PR"
)

@Serializable
data class PartialSession(
    // This is the only field reliably returned by listSessions
    val name: String
)

@Serializable
data class Session(
    val name: String,
    // *** MODIFIED: This is non-nullable per official docs ***
    val id: String,
    val prompt: String,
    val sourceContext: SourceContext,
    val title: String? = null,
    val createTime: String,
    val updateTime: String,
    // Per original context, this API field is unreliable
    val state: String? = null,
    val url: String,
    val outputs: List<SessionOutput>? = null
)

@Serializable
data class ListSessionsResponse(
    // *** MODIFIED: Corrected to use PartialSession per our discovery ***
    val sessions: List<PartialSession>? = null,
    val nextPageToken: String? = null
)

@Serializable
data class SessionOutput(
    val pullRequest: PullRequest? = null
)

@Serializable
data class PullRequest(
    val url: String,
    val title: String,
    val description: String
)

@Serializable
data class ApprovePlanRequest(
    val planId: String? = null // Empty body seems to be what API wants
)

// -----------------------------------------------------------------
// Activities
// -----------------------------------------------------------------

@Serializable
data class SendMessageRequest(
    val prompt: String
)

@Serializable
data class MessageResponse(
    // The API documentation says this is an empty response.
    val status: String? = null
)

@Serializable
data class ListActivitiesResponse(
    val activities: List<Activity>? = null,
    val nextPageToken: String? = null
)

@Serializable
sealed class Activity {
    abstract val name: String
    abstract val id: String
    // *** ADDED: New field from official documentation ***
    abstract val description: String?
    abstract val createTime: String
    abstract val originator: String
    abstract val artifacts: List<Artifact>?

    @Serializable
    @SerialName("agentMessaged")
    data class AgentMessagedActivity(
        override val name: String,
        override val id: String,
        override val description: String? = null,
        override val createTime: String,
        override val originator: String,
        override val artifacts: List<Artifact>? = null,
        val agentMessaged: AgentMessaged
    ) : Activity()

    @Serializable
    @SerialName("userMessaged")
    data class UserMessagedActivity(
        override val name: String,
        override val id: String,
        override val description: String? = null,
        override val createTime: String,
        override val originator: String,
        override val artifacts: List<Artifact>? = null,
        val userMessaged: UserMessaged
    ) : Activity()

    @Serializable
    @SerialName("planGenerated")
    data class PlanGeneratedActivity(
        override val name: String,
        override val id: String,
        override val description: String? = null,
        override val createTime: String,
        override val originator: String,
        override val artifacts: List<Artifact>? = null,
        val planGenerated: PlanGenerated
    ) : Activity()

    @Serializable
    @SerialName("planApproved")
    // *** ADDED: This class was missing, causing the JsonConvertException ***
    data class PlanApprovedActivity(
        override val name: String,
        override val id: String,
        override val description: String? = null,
        override val createTime: String,
        override val originator: String,
        override val artifacts: List<Artifact>? = null,
        val planApproved: PlanApproved
    ) : Activity()

    @Serializable
    @SerialName("progressUpdated")
    data class ProgressUpdatedActivity(
        override val name: String,
        override val id: String,
        override val description: String? = null,
        override val createTime: String,
        override val originator: String,
        override val artifacts: List<Artifact>? = null,
        val progressUpdated: ProgressUpdated
    ) : Activity()

    @Serializable
    @SerialName("sessionCompleted")
    data class SessionCompletedActivity(
        override val name: String,
        override val id: String,
        override val description: String? = null,
        override val createTime: String,
        override val originator: String,
        override val artifacts: List<Artifact>? = null,
        val sessionCompleted: SessionCompleted
    ) : Activity()

    @Serializable
    @SerialName("sessionFailed")
    data class SessionFailedActivity(
        override val name: String,
        override val id: String,
        override val description: String? = null,
        override val createTime: String,
        override val originator: String,
        override val artifacts: List<Artifact>? = null,
        val sessionFailed: SessionFailed
    ) : Activity()
}

// Activity Payloads

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
    val id: String,
    val steps: List<PlanStep>? = null,
    val createTime: String
)

@Serializable
data class PlanStep(
    val id: String,
    val title: String,
    val description: String? = null,
    // *** MODIFIED: Made nullable per our discovery about API unreliability ***
    val index: Int? = null
)

@Serializable
// *** ADDED: This payload class was missing ***
data class PlanApproved(
    val planId: String
)

@Serializable
data class ProgressUpdated(
    val title: String,
    val description: String? = null
)

@Serializable
class SessionCompleted // Empty object

@Serializable
data class SessionFailed(
    val reason: String
)

// Artifacts

@Serializable
data class Artifact(
    val changeSet: ChangeSet? = null,
    val media: Media? = null,
    val bashOutput: BashOutput? = null
)

@Serializable
data class ChangeSet(
    val source: String,
    val gitPatch: GitPatch? = null
)

@Serializable
data class GitPatch(
    val unidiffPatch: String? = null,
    // *** MODIFIED: Made nullable per our discovery about API unreliability ***
    val baseCommitId: String? = null,
    val suggestedCommitMessage: String? = null
)

@Serializable
data class Media(
    val data: String, // Base64-encoded string
    val mimeType: String
)

@Serializable
data class BashOutput(
    val command: String? = null,
    val output: String,
    val exitCode: Int? = null
)