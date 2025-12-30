package ch.hslu.newcmpproject.data.remote.api

import ch.hslu.kanban.data.remote.api.ApiClient
import ch.hslu.kanban.domain.entity.serverRequests.LoginRequest
import ch.hslu.kanban.domain.entity.serverRequests.Token
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class AuthApi {
    suspend fun login(username: String, password: String): Token {
        return try {
            val response = ApiClient.client.post(ApiClient.url("/login")) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }

            if (response.status != HttpStatusCode.OK) return Token("")

            val json = response.body<Map<String, String>>()
            Token(json["token"] ?: "")

        } catch (e: Throwable) {
            Token("")
        }
    }
}
