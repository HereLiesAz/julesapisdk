# **Jules AI Kotlin SDK**

**Note:** This is an **unofficial** community SDK for the Jules AI API. It is not affiliated with, officially maintained by, or endorsed by Google.

A fully-typed Kotlin SDK for the [Jules AI API](https://developers.google.com/jules/api), built with Ktor and Kotlinx Serialization. [Jules](https://jules.google.com) is a Google product that provides AI-powered coding assistance.

This SDK provides a simple, asynchronous, and type-safe way to interact with all Jules API endpoints from any Kotlin application. It features a robust SdkResult wrapper and a stateful JulesSession object to simplify API interaction.

**SDK Version:** 1.0.2 (implementing Jules API v1alpha, last updated 2025-11-12 UTC)

## **Installation**

Add the following dependency to your build.gradle.kts or build.gradle file:

**Gradle (Kotlin DSL)**

implementation("com.hereliesaz.julesapisdk:kotlin-sdk:1.0.2")

## **Quick Start for Android**

All SDK methods are suspend functions and are main-safe. They should be called from a CoroutineScope, such as the viewModelScope in an Android ViewModel.

This example shows the full, correct flow for creating a session, sending a message, and getting the response.

import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.hereliesaz.julesapisdk.JulesClient  
import com.hereliesaz.julesapisdk.JulesSession  
import com.hereliesaz.julesapisdk.CreateSessionRequest  
import com.hereliesaz.julesapisdk.SourceContext  
import com.hereliesaz.julesapisdk.SdkResult  
import kotlinx.coroutines.launch

class MyViewModel(private val julesClient: JulesClient) : ViewModel() {

    private var activeSession: JulesSession? \= null

    fun startSessionAndSendMessage(prompt: String) {  
        viewModelScope.launch { // Always use a scope like viewModelScope  
              
            // 1\. Create the session  
            val sessionRequest \= CreateSessionRequest(  
                prompt \= "Create a boba app\!",  
                sourceContext \= SourceContext(source \= "sources/github/my-org/my-repo"),  
                title \= "Boba App"  
            )

            val sessionResult \= julesClient.createSession(sessionRequest)  
              
            val julesSession \= when (sessionResult) {  
                is SdkResult.Success \-\> {  
                    addLog("Created session: ${sessionResult.data.session.name}")  
                    sessionResult.data // This is our new JulesSession object  
                }  
                is SdkResult.Error \-\> {  
                    addLog("API Error: ${sessionResult.code} \- ${sessionResult.body}")  
                    return@launch  
                }  
                is SdkResult.NetworkError \-\> {  
                    addLog("Network Error: ${sessionResult.throwable.message}")  
                    return@launch  
                }  
            }  
              
            activeSession \= julesSession

            // 2\. Send a follow-up message  
            addLog("Sending message: $prompt")  
            when (val messageResult \= julesSession.sendMessage(prompt)) {  
                is SdkResult.Success \-\> {  
                    addLog("Message sent. Polling for activities...")  
                    // 3\. IMPORTANT: Poll for activities to see the response  
                    pollActivities(julesSession)  
                }  
                is SdkResult.Error \-\> addLog("Error sending message: ${messageResult.code} \- ${messageResult.body}")  
                is SdkResult.NetworkError \-\> addLog("Network error: ${messageResult.throwable.message}")  
            }  
        }  
    }  
      
    private suspend fun pollActivities(session: JulesSession) {  
        when (val activityResult \= session.listActivities()) {  
            is SdkResult.Success \-\> {  
                val activities \= activityResult.data.activities ?: emptyList()  
                addLog("Got ${activities.size} activities.")  
                // Update UI with the full list of activities  
            }  
            is SdkResult.Error \-\> addLog("Error polling activities: ${activityResult.code} \- ${activityResult.body}")  
            is SdkResult.NetworkError \-\> addLog("Network error: ${activityResult.throwable.message}")  
        }  
    }  
      
    private fun addLog(message: String) { /\* ... \*/ }  
}

## **Core Concepts & API Flow**

The Jules API has several non-obvious behaviors. This SDK is designed to help you navigate them.

### **1\. JulesClient vs. JulesSession**

* **JulesClient** is a factory and discovery tool. You use it to:
    * listSources()
    * listSessions()
    * createSession() (which *returns* a JulesSession)
    * getSession() (which *returns* a JulesSession)
* **JulesSession** is a stateful object for *interaction*. Once you have a JulesSession object, you call methods on **it**, not the client:
    * julesSession.sendMessage(...)
    * julesSession.listActivities(...)
    * julesSession.approvePlan()

### **2\. The Asynchronous Messaging Flow**

**sendMessage is fire-and-forget.** When you call julesSession.sendMessage(prompt), it only *queues* your message. The API returns an empty response immediately.

To see your message appear in the chat *and* to get the agent's reply, you **must** call julesSession.listActivities() *after* sendMessage returns.

1. julesSession.sendMessage("Do the thing") \-\> Returns SdkResult.Success(Unit)
2. julesSession.listActivities() \-\> Returns SdkResult.Success(ListActivitiesResponse(...)) which *now contains* your "Do the thing" message and any new agent responses.

### **3\. Resuming a Session**

The `julesClient.listSessions()` endpoint returns a list of **full, complete** `Session` objects. The workflow to resume a session is straightforward:

1.  `julesClient.listSessions()` -> Get a list of *full* sessions.
2.  User clicks a session, providing you with a `Session` object (`sessionFromList`).
3.  Instantiate a `JulesSession` using that object: `val julesSession = JulesSession(julesClient, sessionFromList)`
4.  Call methods on the new object: `julesSession.listActivities()` -> This will **succeed**.

*(Note: Previous guidance incorrectly stated that `listSessions` returned partial objects that would cause a 404 error. This was based on a misunderstanding and has been corrected. You do **not** need to call `getSession` before `listActivities` when resuming.)*

## **Android Test App**

The included Android test app in the `android-test-app` directory provides a simple way to test the SDK's functionality.

### **Configuring the Test App**

1. **API Key**: Enter your Jules API key in the "API Key" field in the app's settings.
2. **Load Data**: Press the "Load Data" button.
3. The app will load two lists:
    * **Existing Sessions**: Click any session to resume it and load its chat history in the "Chat" tab.
    * **Repositories**: Click any repository to create a **new** session and be taken to the "Chat" tab.

## **API Reference**

All methods return a `suspend` function that returns an `SdkResult<T>`.

### **SdkResult\<T\> Wrapper**

Every API call returns one of three states:

* `SdkResult.Success(data: T)`: The call was successful. The `data` property holds the response object (e.g., a `Session` or `ListActivitiesResponse`).
* `SdkResult.Error(code: Int, body: String)`: The API returned an HTTP error (e.g., 401, 404, 500).
* `SdkResult.NetworkError(throwable: Throwable)`: The request failed due to a network issue or a client-side crash (like a deserialization error).

### **JulesClient (Factory & Discovery)**

#### **Constructor**

val client \= JulesClient(  
apiKey \= "YOUR\_API\_KEY",  
baseUrl \= "\[https://jules.googleapis.com\](https://jules.googleapis.com)", // Optional  
apiVersion \= "v1alpha" // Optional  
)

#### **Methods**

##### **listSources(pageSize: Int?, pageToken: String?, filter: String?): SdkResult\<ListSourcesResponse\>**

List all available sources.

##### **getSource(sourceId: String): SdkResult\<GithubRepoSource\>**

Get details of a specific source.

##### **createSession(request: CreateSessionRequest): SdkResult\<JulesSession\>**

Create a new session. **Returns a JulesSession object on success.**

##### **listSessions(pageSize: Int?, pageToken: String?): SdkResult\<ListSessionsResponse\>**

List all sessions.

##### **getSession(sessionId: String): SdkResult\<JJulesSession\>**

Get details of a specific session. **Returns a JulesSession object on success.**

### **JulesSession (Interaction)**

You get this object from `createSession()` or `getSession()`.

#### **Methods**

##### **sendMessage(prompt: String): SdkResult\<MessageResponse\>**

Send a message to the agent in this session. This is asynchronous.

##### **listActivities(pageSize: Int?, pageToken: String?): SdkResult\<ListActivitiesResponse\>**

List activities for this session. Call this after `sendMessage` to get new messages.

##### **getActivity(activityId: String): SdkResult\<Activity\>**

Get a specific activity for this session.

##### **approvePlan(): SdkResult\<Unit\>**

Approve the latest plan for this session.

##### **refreshSessionState(): SdkResult\<Session\>**

Fetches the latest state for this session object (e.g., to check if `state` has changed to `COMPLETED`).

## **Error Handling & Resilience**

### **Automatic Retries**

Retries are **disabled by default**. To enable them, provide a `RetryConfig` with `maxRetries` greater than 0.

val client \= JulesClient(  
apiKey \= "YOUR\_API\_KEY",  
retryConfig \= RetryConfig(  
maxRetries \= 5,  
initialDelayMs \= 500  
)  
)

### **v1alpha Stability & Deserialization**

The `v1alpha` API is experimental and does not always return a consistent schema. This can cause deserialization crashes in strongly-typed clients.

The SDK's models (in `Schemas.kt`) have been made **nullable** in known problem fields to prevent crashes.

* `Session.state` is nullable.
* `GithubRepo.isPrivate` is nullable.

If you encounter a `NetworkError` containing a `kotlinx.serialization.MissingFieldException`, it means the API has changed again. Please update `Schemas.kt` to make the reported field nullable and file an issue.

## **Resources**

* [Official Jules API Documentation](https://developers.google.com/jules/api)
* [Jules Web App](https://jules.google.com)
* [GitHub Repository](https://github.com/hereliesaz/jules-api-sdk)

## **License**

This project is licensed under the MIT License \- see the [LICENSE](https://www.google.com/search?q=LICENSE) file for details.