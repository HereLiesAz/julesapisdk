package com.jules.sdk

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

data class RetryConfig(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 1000
)

class HttpClient(
    private val apiKey: String,
    private val baseUrl: String = "https://jules.googleapis.com/v1alpha",
    private val timeout: Long = 30000,
    private val retryConfig: RetryConfig = RetryConfig()
) {
    val client = HttpClient(CIO) {
        engine {
            requestTimeout = timeout
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(retryConfig.maxRetries)
            exponentialDelay(retryConfig.initialDelayMs.toDouble())
            retryIf { _, response ->
                response.status.value.let { it in setOf(408, 429, 500, 502, 503, 504) }
            }
        }
        defaultRequest {
            url(baseUrl)
            header("X-Goog-Api-Key", apiKey)
            contentType(ContentType.Application.Json)
        }
    }

    suspend inline fun <reified T> get(endpoint: String, params: Map<String, String> = emptyMap()): T {
        return client.get(endpoint) {
            params.forEach { (key, value) ->
                parameter(key, value)
            }
        }.body()
    }

    suspend inline fun <reified T> postAndReceive(endpoint: String, body: Any): T {
        return client.post(endpoint) {
            setBody(body)
        }.body()
    }

    suspend fun postWithBody(endpoint: String, body: Any) {
        client.post(endpoint) {
            setBody(body)
        }
    }

    suspend fun post(endpoint: String) {
        client.post(endpoint)
    }
}
