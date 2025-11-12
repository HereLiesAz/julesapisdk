package com.jules.sdk

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `can deserialize agentMessaged activity`() {
        val activityJson = """
            {
                "name": "activity1",
                "id": "1",
                "createTime": "2025-01-01T00:00:00Z",
                "updateTime": "2025-01-01T00:00:00Z",
                "description": "Agent messaged",
                "state": "COMPLETED",
                "agentMessaged": {
                    "agentMessage": "Hello from the agent"
                }
            }
        """.trimIndent()
        val activity = json.decodeFromString<Activity>(activityJson)
        assertNotNull(activity.agentMessaged)
        assertEquals("Hello from the agent", activity.agentMessaged?.agentMessage)
    }

    @Test
    fun `can deserialize planGenerated activity`() {
        val activityJson = """
            {
                "name": "activity2",
                "id": "2",
                "createTime": "2025-01-01T00:00:00Z",
                "updateTime": "2025-01-01T00:00:00Z",
                "description": "Plan generated",
                "state": "COMPLETED",
                "planGenerated": {
                    "plan": {
                        "steps": [
                            { "description": "Step 1" },
                            { "description": "Step 2" }
                        ]
                    }
                }
            }
        """.trimIndent()
        val activity = json.decodeFromString<Activity>(activityJson)
        assertNotNull(activity.planGenerated)
        assertEquals(2, activity.planGenerated?.plan?.steps?.size)
    }

    @Test
    fun `can deserialize changeSet artifact`() {
        val artifactJson = """
            {
                "name": "artifact1",
                "id": "1",
                "createTime": "2025-01-01T00:00:00Z",
                "updateTime": "2025-01-01T00:00:00Z",
                "changeSet": {
                    "source": "file.kt",
                    "patch": "@@ -1,1 +1,1 @@"
                }
            }
        """.trimIndent()
        val artifact = json.decodeFromString<Artifact>(artifactJson)
        assertNotNull(artifact.changeSet)
        assertEquals("file.kt", artifact.changeSet?.source)
    }
}
