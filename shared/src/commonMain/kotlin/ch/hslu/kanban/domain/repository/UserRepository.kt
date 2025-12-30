package ch.hslu.kanban.domain.repository

import ch.hslu.kanban.data.remote.api.UserApi
import ch.hslu.kanban.domain.entity.messages.UpdateUsernameResult
import ch.hslu.kanban.domain.entity.serverRequests.Token
import ch.hslu.kanban.domain.entity.serverRequests.UserSimple
import ch.hslu.kanban.network.AuthService

class UserRepository(
    private val userApi: UserApi,
    private val authService: AuthService
) {
    private val token: Token?
        get() = authService.token

    suspend fun addUser(username: String, password: String, role: String): Boolean {
        val currentToken = token ?: return false
        return userApi.addUser(currentToken, username, password, role)
    }

    suspend fun getAllUsers(): List<UserSimple> {
        val currentToken = token ?: return emptyList()
        return userApi.getAllUsers(currentToken)
    }

    suspend fun getUserWithId(userId: Long): UserSimple? {
        val currentToken = token ?: return null
        return userApi.getUserWithId(currentToken, userId)
    }

    suspend fun updateUsername(
        userId: Long?,
        newUsername: String
    ): UpdateUsernameResult
    {
        val currentToken = token ?: return UpdateUsernameResult(false, null)
        val result = userApi.updateUsername(currentToken, userId, newUsername)

        // Self-update: Token speichern
        result.token?.let { newToken -> authService.updateToken(newToken) }

        return result
    }

    suspend fun updatePassword(
        userId: Long?,
        oldPassword: String?,
        newPassword: String
    ): Boolean {
        val currentToken = token ?: return false
        return userApi.updatePassword(
            currentToken,
            userId,
            oldPassword,
            newPassword
        )
    }

    suspend fun deleteUser(userId: Long): Boolean {
        val currentToken = token ?: return false
        return userApi.deleteUser(currentToken, userId)
    }
}