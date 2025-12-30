package ch.hslu.kanban.data.remote.api

import ch.hslu.kanban.SERVER_IP
import ch.hslu.kanban.domain.entity.messages.UpdateUsernameResult
import ch.hslu.kanban.domain.entity.serverRequests.CreateUserRequest
import ch.hslu.kanban.domain.entity.serverRequests.LoginRequest
import ch.hslu.kanban.domain.entity.serverRequests.Token
import ch.hslu.kanban.domain.entity.serverRequests.UpdatePasswordRequest
import ch.hslu.kanban.domain.entity.serverRequests.UpdateUsernameRequest
import ch.hslu.kanban.domain.entity.serverRequests.UserSimple
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class UserApi {
    suspend fun addUser(
        token: Token,
        username: String,
        password: String,
        role: String = "USER"
    ): Boolean = try {
        val response = ApiClient.client.post("${SERVER_IP}/users") {
            header("Authorization", "Bearer ${token.value}")
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username, password, role))
        }

        response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    suspend fun getAllUsers(token: Token): List<UserSimple> {
        return try {
            ApiClient.client.get("${SERVER_IP}/users") {
                header("Authorization", "Bearer ${token.value}")
            }.body<List<UserSimple>>()
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserWithId(
        token: Token,
        userId: Long
    ): UserSimple? {
        return try {
            ApiClient.client.get("${SERVER_IP}/users/$userId") {
                header("Authorization", "Bearer ${token.value}")
            }.body<UserSimple>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    suspend fun updateUsername(
        token: Token,
        userId: Long?,
        newUsername: String
    ): UpdateUsernameResult
    {
        val response = ApiClient.client.put("${SERVER_IP}/user/username") {
            header("Authorization", "Bearer ${token.value}")
            contentType(ContentType.Application.Json)
            setBody(
                UpdateUsernameRequest(
                    userId = userId,
                    username = newUsername
                )
            )
        }

        if (response.status != HttpStatusCode.OK)
            return UpdateUsernameResult(false, null)

        val json = response.body<Map<String, String>>()
        return UpdateUsernameResult(true, json["token"])
    }

    suspend fun updatePassword(
        token: Token,
        userId: Long?,
        oldPassword: String?,
        newPassword: String
    ): Boolean {
        val response = ApiClient.client.put("${SERVER_IP}/user/password") {
            header("Authorization", "Bearer ${token.value}")
            contentType(ContentType.Application.Json)
            setBody(
                UpdatePasswordRequest(
                    userId = userId,
                    newPassword = newPassword,
                    oldPassword = oldPassword
                )
            )
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun deleteUser(token: Token, userId: Long): Boolean {
        return try {
            val response = ApiClient.client.delete("${SERVER_IP}/users/$userId") {
                header("Authorization", "Bearer ${token.value}")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }


}
