package ch.hslu.kanban.domain.repository

import ch.hslu.kanban.data.remote.api.UserApi
import ch.hslu.kanban.domain.entity.messages.UpdateUsernameResult
import ch.hslu.kanban.domain.entity.serverRequests.UserSimple
import ch.hslu.kanban.network.AuthService

class UserRepository(
    private val userApi: UserApi,
    private val authService: AuthService
) {

    suspend fun addUser(
        username: String,
        password: String,
        role: String = "USER"
    ): Boolean =
        authService.withSession { token, _ ->
            userApi.addUser(token, username, password, role)
        } ?: false

    suspend fun getAllUsers(): List<UserSimple> =
        authService.withSession { token, _ ->
            userApi.getAllUsers(token)
        } ?: emptyList()

    suspend fun getUser(userId: Long): UserSimple? =
        authService.withSession { token, _ ->
            userApi.getUserWithId(token, userId)
        }

    suspend fun updateUsername(userId: Long?, newUsername: String): UpdateUsernameResult? {
        return authService.withSession { token, currentUserId ->
            val targetUserId = userId ?: currentUserId
            val result = userApi.updateUsername(token, targetUserId, newUsername)

            // Falls das Token zurückgegeben wurde (Self-Update), speichern
            result.token?.let { newToken ->
                authService.updateToken(newToken)
            }

            result
        }
    }

    suspend fun updatePassword(
        userId: Long?,
        oldPassword: String?,
        newPassword: String
    ): Boolean =
        authService.withSession { token, _ ->
            userApi.updatePassword(
                token, userId, oldPassword, newPassword
            )
        } ?: false

    suspend fun deleteUser(userId: Long): Boolean =
        authService.withSession { token, _ ->
            userApi.deleteUser(token, userId)
        } ?: false
}
