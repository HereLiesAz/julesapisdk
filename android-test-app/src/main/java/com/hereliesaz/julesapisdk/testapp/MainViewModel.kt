package com.hereliesaz.julesapisdk.testapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.julesapisdk.Activity
import com.hereliesaz.julesapisdk.ApiConfig
import com.hereliesaz.julesapisdk.CreateSessionRequest
import com.hereliesaz.julesapisdk.GithubRepoSource
import com.hereliesaz.julesapisdk.GithubRepoContext
// *** MODIFIED: Renamed import to use the new facade ***
import com.hereliesaz.julesapisdk.Jules
import com.hereliesaz.julesapisdk.JulesSession
import com.hereliesaz.julesapisdk.PartialSession
import com.hereliesaz.julesapisdk.SdkResult
import com.hereliesaz.julesapisdk.Source
import com.hereliesaz.julesapisdk.SourceContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    // A single StateFlow to hold the UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // For Chat tab
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // For Logcat tab
    private val _diagnosticLogs = MutableStateFlow<List<String>>(emptyList())
    val diagnosticLogs: StateFlow<List<String>> = _diagnosticLogs

    // *** MODIFIED: Use the new Jules facade ***
    private var julesFacade: Jules? = null
    private var julesSession: JulesSession? = null

    // State to track if the session is active
    private var isSessionActive = false

    fun addLog(log: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val currentLogs = _diagnosticLogs.value.toMutableList()
        currentLogs.add("$timestamp: $log")
        _diagnosticLogs.value = currentLogs
    }

    fun initializeClient(apiKey: String) {
        if (apiKey.isNotBlank()) {
            // *** MODIFIED: Construct Jules facade with ApiConfig ***
            julesFacade = Jules(ApiConfig(apiKey))
            addLog("Jules facade initialized.")
        } else {
            addLog("Attempted to initialize client with blank API key.")
        }
    }

    fun loadSettingsData() {
        if (julesFacade == null) {
            _uiState.value = UiState.Error("API Key is not set. Cannot load data.")
            return
        }
        addLog("Loading sessions and sources...")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Run both calls in parallel
                val (sessionsResult, sourcesResult) = coroutineScope {
                    // *** MODIFIED: Access via facade.api ***
                    val sessionsJob = async { julesFacade!!.api.listSessions() }
                    val sourcesJob = async { julesFacade!!.api.listSources() }
                    Pair(sessionsJob.await(), sourcesJob.await())
                }

                // Check results
                val sessions = when (sessionsResult) {
                    is SdkResult.Success -> sessionsResult.data.sessions ?: emptyList()
                    is SdkResult.Error -> throw Exception("Sessions Error: ${sessionsResult.code} - ${sessionsResult.body}")
                    is SdkResult.NetworkError -> throw Exception("Sessions Network Error: ${sessionsResult.throwable.message}")
                }

                val sources = when (sourcesResult) {
                    is SdkResult.Success -> sourcesResult.data.sources ?: emptyList()
                    is SdkResult.Error -> throw Exception("Sources Error: ${sourcesResult.code} - ${sourcesResult.body}")
                    is SdkResult.NetworkError -> throw Exception("Sources Network Error: ${sourcesResult.throwable.message}")
                }

                // Emit the new combined state
                _uiState.value = UiState.SettingsLoaded(sessions, sources)
                addLog("Successfully loaded ${sessions.size} sessions and ${sources.size} sources.")

            } catch (e: Exception) {
                val errorMsg = "Error loading settings data: ${e.message}"
                _uiState.value = UiState.Error(errorMsg)
                addLog(errorMsg)
            }
        }
    }

    // *** MODIFIED: This function now takes a PartialSession and implements the getSession() fix ***
    fun resumeSession(partialSession: PartialSession) {
        if (julesFacade == null) {
            addLog("Error: Client not initialized. Cannot resume session.")
            return
        }
        addLog("Resuming partial session: ${partialSession.name}...")
        _uiState.value = UiState.Loading // Show spinner
        viewModelScope.launch {
            _messages.value = emptyList() // Clear chat

            // *** FIX: Call getSession to fetch the full Session object ***
            when (val getSessionResult = julesFacade!!.api.getSession(partialSession.name)) {
                is SdkResult.Success -> {
                    // Now we have the full session, wrap it and proceed
                    val fullSession = getSessionResult.data
                    julesSession = fullSession
                    isSessionActive = (fullSession.session.state != "COMPLETED" && fullSession.session.state != "FAILED")

                    val successMsg = "Resumed session: ${fullSession.session.name}\nState: ${fullSession.session.state ?: "UNKNOWN"}"
                    addMessage(Message(successMsg, MessageType.BOT))
                    addLog(successMsg)

                    _uiState.value = UiState.Idle // Hide spinner
                    loadActivities() // Load the history
                }
                is SdkResult.Error -> {
                    val errorMsg = "API Error resuming session: ${getSessionResult.code} - ${getSessionResult.body}"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                    _uiState.value = UiState.Error(errorMsg)
                }
                is SdkResult.NetworkError -> {
                    val sw = StringWriter()
                    getSessionResult.throwable.printStackTrace(PrintWriter(sw))
                    val errorMsg = "Network error resuming session:$sw"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                    _uiState.value = UiState.Error(errorMsg)
                }
            }
        }
    }

    // This function is called when clicking a new source
    fun createSession(source: Source) {
        if (julesFacade == null) {
            addLog("Error: API Key is not set. Cannot create session.")
            return
        }
        addLog("Creating session with source: ${source.name}")
        viewModelScope.launch {
            _messages.value = emptyList() // Clear chat on new session
            val sourceContext = if (source is GithubRepoSource) {
                SourceContext(source.name, GithubRepoContext("main"))
            } else {
                SourceContext(source.name)
            }
            _uiState.value = UiState.Loading
            // *** MODIFIED: Access via facade.api ***
            when (val result = julesFacade?.api?.createSession(CreateSessionRequest("Test Application", sourceContext))) {
                is SdkResult.Success -> {
                    julesSession = result.data
                    isSessionActive = true
                    if (source is GithubRepoSource) {
                        val url = "https://github.com/${source.githubRepo.owner}/${source.githubRepo.repo}"
                        val successMsg = "Session created with source: $url"
                        addMessage(Message(successMsg, MessageType.BOT))
                        addLog(successMsg)
                    } else {
                        addLog("Session created with source: ${source.name} (URL not available)")
                    }
                    // We don't need to change UI state, just load activities
                    _uiState.value = UiState.Idle
                    loadActivities()
                }
                is SdkResult.Error -> {
                    val errorMsg = "API Error creating session: ${result.code} - ${result.body}"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                    _uiState.value = UiState.Error(errorMsg)
                }
                is SdkResult.NetworkError -> {
                    val sw = StringWriter()
                    result.throwable.printStackTrace(PrintWriter(sw))
                    val errorMsg = "Network error creating session:$sw"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                    _uiState.value = UiState.Error(errorMsg)
                }
                null -> {
                    addLog("Error: Jules facade is not initialized.")
                    _uiState.value = UiState.Error("Jules facade is not initialized.")
                }
            }
        }
    }

    // --- DEPRECATED FUNCTIONS (no longer called) ---
    fun loadSessions() {
        addLog("WARNING: 'loadSessions' is deprecated. Use 'loadSettingsData'.")
    }
    fun loadSources() {
        addLog("WARNING: 'loadSources' is deprecated. Use 'loadSettingsData'.")
    }


    private fun loadActivities() {
        if (julesSession == null) {
            addLog("Cannot load activities: Session not initialized.")
            return
        }
        addLog("Loading activities...")
        viewModelScope.launch {
            // *** MODIFIED: Call listActivities on the session object ***
            when (val result = julesSession?.listActivities()) {
                is SdkResult.Success -> {
                    val newMessages = mutableListOf<Message>()
                    val activities = result.data.activities ?: emptyList()

                    for (activity in activities) {
                        when (activity) {
                            is Activity.UserMessagedActivity -> newMessages.add(Message(activity.userMessaged.userMessage, MessageType.USER))
                            is Activity.AgentMessagedActivity -> newMessages.add(Message(activity.agentMessaged.agentMessage, MessageType.BOT))
                            is Activity.PlanGeneratedActivity -> {
                                // *** MODIFIED: Handle nullable index ***
                                val planText = "Plan Generated:\n" + activity.planGenerated.plan.steps?.joinToString("\n") { "  ${(it.index ?: 0) + 1}. ${it.title}" }
                                newMessages.add(Message(planText, MessageType.BOT))
                            }
                            is Activity.ProgressUpdatedActivity -> newMessages.add(Message("[BOT: ${activity.progressUpdated.title}]", MessageType.BOT))
                            is Activity.SessionCompletedActivity -> {
                                newMessages.add(Message("[BOT: Session Completed]", MessageType.BOT))
                                isSessionActive = false
                            }
                            is Activity.SessionFailedActivity -> {
                                newMessages.add(Message("[BOT: Session Failed - ${activity.sessionFailed.reason}]", MessageType.ERROR))
                                isSessionActive = false
                            }
                            is Activity.PlanApprovedActivity -> newMessages.add(Message("[USER: Plan Approved]", MessageType.USER))
                            // Handle unknown activities gracefully
                            else -> addLog("Ignored unknown activity.")
                        }
                    }
                    _messages.value = newMessages
                    addLog("Successfully loaded ${activities.size} activities.")
                }
                is SdkResult.Error -> {
                    val errorMsg = "API Error loading activities: ${result.code} - ${result.body}"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                }
                is SdkResult.NetworkError -> {
                    val sw = StringWriter()
                    result.throwable.printStackTrace(PrintWriter(sw))
                    val errorMsg = "Network error loading activities:$sw"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                }
                null -> {}
            }
        }
    }

    fun sendMessage(text: String) {
        if (julesSession == null) {
            val errorMsg = "Session not created. Please configure API Key and Source in Settings."
            addMessage(Message(errorMsg, MessageType.ERROR))
            addLog(errorMsg)
            return
        }

        if (!isSessionActive) {
            val errorMsg = "Session is closed. Please create a new session in Settings to continue."
            addMessage(Message(errorMsg, MessageType.ERROR))
            addLog(errorMsg)
            return
        }

        addLog("Sending message: $text")

        viewModelScope.launch {
            // *** MODIFIED: Call sendMessage on the session object ***
            when (val result = julesSession?.sendMessage(text)) {
                is SdkResult.Success -> {
                    addLog("Message sent successfully. Refreshing activities...")
                    // *** FIX: Poll activities to see our own message and the reply ***
                    loadActivities()
                }
                is SdkResult.Error -> {
                    val errorMsg = "Error sending message: ${result.code} - ${result.body}"
                    addLog(errorMsg)
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    if (result.code == 404) isSessionActive = false
                }
                is SdkResult.NetworkError -> {
                    val sw = StringWriter()
                    result.throwable.printStackTrace(PrintWriter(sw))
                    val errorMsg = "Network error sending message:$sw"
                    addLog(errorMsg)
                    addMessage(Message(errorMsg, MessageType.ERROR))
                }
                null -> addLog("Error: Session is not initialized.")
            }
        }
    }

    private fun addMessage(message: Message) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
}