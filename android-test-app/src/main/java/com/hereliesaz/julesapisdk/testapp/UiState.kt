package com.hereliesaz.julesapisdk.testapp

import com.hereliesaz.julesapisdk.Session
import com.hereliesaz.julesapisdk.Source

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    // This new state holds both lists
    data class SettingsLoaded(
        val sessions: List<Session>,
        val sources: List<Source>
    ) : UiState()
    data class Error(val message: String) : UiState()

    // --- DEPRECATED STATES (no longer used by SettingsFragment) ---
    data class SourcesLoaded(val sources: List<Source>) : UiState()
    data class SessionsLoaded(val sessions: List<Session>) : UiState()
}