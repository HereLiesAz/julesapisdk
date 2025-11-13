package com.hereliesaz.julesapisdk

/**
 * A sealed class representing the result of an SDK operation.
 *
 * This class is used to encapsulate the result of an API call, which can be
 * either a success, an API error, or a network error.
 *
 * @param T The type of the successful result data.
 */
sealed class SdkResult<out T> {
    /**
     * Represents a successful API call.
     *
     * @property data The data returned by the API.
     */
    data class Success<out T>(val data: T) : SdkResult<T>()

    /**
     * Represents a failed API call (e.g., 4xx or 5xx response).
     *
     * @property code The HTTP status code of the error response.
     * @property body The raw error body returned by the API.
     */
    data class Error(val code: Int, val body: String) : SdkResult<Nothing>()

    /**
     * Represents a network or client-side error (e.g., connection timeout,
     * deserialization issue).
     *
     * @property throwable The underlying exception that occurred.
     */
    data class NetworkError(val throwable: Throwable) : SdkResult<Nothing>()
}