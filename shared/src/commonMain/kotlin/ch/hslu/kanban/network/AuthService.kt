package ch.hslu.kanban.network

import ch.hslu.kanban.domain.cache.TokenStorage
import ch.hslu.kanban.domain.cache.UserStorage
import ch.hslu.kanban.domain.entity.serverRequests.Token
import ch.hslu.kanban.domain.entity.serverRequests.UserSimple
import ch.hslu.kanban.domain.entity.serverRequests.decodeBase64UrlToString
import ch.hslu.newcmpproject.data.remote.api.AuthApi
import kotlinx.serialization.json.Json

class AuthService(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val userStorage: UserStorage
) {

    private val json = Json { ignoreUnknownKeys = true }

    val token: Token?
        get() = tokenStorage.loadToken()?.let { Token(it) }

    val currentUser: UserSimple?
        get() = userStorage.loadUser()


    suspend fun login(username: String, password: String): Boolean {
        val token = authApi.login(username, password)
        if (token.value.isBlank()) return false

        tokenStorage.saveToken(token.value)

        val user = extractUserFromToken(token.value) ?: return false
        userStorage.saveUser(user)

        return true
    }

    fun logout() {
        tokenStorage.clearToken()
        userStorage.clearUser()
    }

    fun updateToken(newToken: String) {
        tokenStorage.saveToken(newToken)

        extractUserFromToken(newToken)?.let {
            userStorage.saveUser(it)
        }
    }

    private fun extractUserFromToken(token: String): UserSimple? {
        val parts = token.split(".")
        if (parts.size < 2) return null

        val decodedJson = decodeBase64UrlToString(parts[1])
        return try {
            json.decodeFromString<UserSimple>(decodedJson)
        } catch (e: Exception) {
            null
        }
    }
}