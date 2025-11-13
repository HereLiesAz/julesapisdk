# **Understanding the Jules Agent**

In the Jules API, the "Agent" is the AI entity that performs the work for a given Session. It is not a resource you can call directly, but rather an asynchronous actor that you interact with by creating and modifying the Session state.

## **The Agent Lifecycle (via Session State)**

The Agent's lifecycle is directly reflected by the Session.state field. Understanding this flow is critical to building a responsive application.

1. **QUEUED**: The Session has been created, but the Agent has not yet started work.
2. **PLANNING**: The Agent is actively analyzing the prompt and source code to generate a plan.
3. **AWAITING\_PLAN\_APPROVAL**: The Agent has produced a plan (visible in a planGenerated activity) and is now paused, waiting for the user to call the :approvePlan endpoint.
    * *Note: This state is only entered if the Session was created with requirePlanApproval: true.*
4. **IN\_PROGRESS**: The Agent is actively executing the steps of its plan (e.g., writing code, running commands). It will post progressUpdated activities during this time.
5. **AWAITING\_USER\_FEEDBACK**: The Agent has encountered a problem or needs clarification and is paused, waiting for a sendMessage call from the user.
6. **FAILED**: The Agent has encountered an unrecoverable error and has stopped. The sessionFailed activity will contain the reason.
7. **COMPLETED**: The Agent has successfully finished its task. The sessionCompleted activity will contain the final artifacts (like a pull request).

Once a session is in a terminal state (FAILED or COMPLETED), it is considered "closed," and the Agent can no longer be interacted with.

## **How to Interact with the Agent**

All interaction is asynchronous and mediated by the Session.

### **1\. Giving Instructions (Sending a Message)**

You send instructions to the Agent using the sessions.sendMessage endpoint.

* **POST /v1alpha/{session=sessions/\*}:sendMessage**

This call is "fire-and-forget." It simply queues your message and returns an empty response. The Agent will process this message and respond with new activities.

### **2\. Receiving Communication (Listing Activities)**

The Agent *only* communicates back to you by creating new Activity objects within the Session. You must poll the sessions.activities.list endpoint to see these communications.

* **GET /v1alpha/{parent=sessions/\*}/activities**

The Agent uses several Activity types to communicate:

* **agentMessaged**: The Agent is sending a simple text message to the user.
* **planGenerated**: The Agent is presenting its plan for approval.
* **progressUpdated**: The Agent is reporting on a step it has just completed.
* **sessionFailed**: The Agent is reporting that it has failed.
* **sessionCompleted**: The Agent is reporting that it has finished its work.