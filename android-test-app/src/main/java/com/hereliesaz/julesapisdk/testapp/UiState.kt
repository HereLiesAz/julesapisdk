package com.hereliesaz.julesapisdk.testapp

import com.hereliesaz.julesapisdk.PartialSession
import com.hereliesaz.julesapisdk.Session
import com.hereliesaz.julesapisdk.Source

/**
 * Represents the different states of the UI.
 * This single sealed class replaces the multiple LiveData objects for state management.
 */
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()

    // *** MODIFIED: This now holds PartialSession objects from listSessions ***
    data class SettingsLoaded(val sessions: List<PartialSession>, val sources: List<Source>) : UiState()

    // Deprecated states, kept for compatibility during refactor if needed
    data class SessionsLoaded(val sessions: List<Session>) : UiState()
    data class SourcesLoaded(val sources: List<Source>) : UiState()

    data class Error(val message: String) : UiState()
}