package ch.hslu.kanban.network

import ch.hslu.kanban.SERVER_IP
import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.data.remote.api.ApiClient
import ch.hslu.kanban.data.remote.api.TaskApi
import ch.hslu.kanban.domain.entity.serverRequests.Token
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SyncService(
    private val taskApi: TaskApi,
    private val taskDao: TaskDao,
    private val authService: AuthService
) {
    private val _isServerOnline = MutableStateFlow(false)
    val isServerOnline: StateFlow<Boolean> = _isServerOnline

    private val _isInSync = MutableStateFlow(true)
    val isInSync: StateFlow<Boolean> = _isInSync


    suspend fun checkServerStatus() {
        val online = isServerOnline()
        _isServerOnline.value = online

        if (online) updateSyncState()
    }

    suspend fun isServerOnline(): Boolean {
        return try {
            val response = ApiClient.client.get("$SERVER_IP/health")
            return response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            false
        }
    }

    private inline fun <T> withSession(block: (token: Token, userId: Long) -> T): T? {
        val token = authService.token ?: return null
        val userId = authService.currentUser?.userId ?: return null
        return block(token, userId)
    }

    suspend fun updateSyncState() {
        withSession { token, userId ->
            val serverTasks = taskApi.getTasks(token)
            val localTasks = taskDao.getAll(userId)
            _isInSync.value =
                localTasks.sortedBy { it.id } == serverTasks.sortedBy { it.id }
        }
    }

    suspend fun pull(): Boolean =
        withSession { token, userId ->
            if (!isServerOnline.value) return@withSession false

            val serverTasks = taskApi.getTasks(token)
            if (serverTasks.isEmpty()) return@withSession false

            taskDao.replaceAll(userId, serverTasks)
            true
        } ?: false

    suspend fun push(): Boolean =
        withSession { token, userId ->
            if (!isServerOnline.value) return@withSession false
            taskApi.replaceTasks(token, taskDao.getAll(userId))
        } ?: false

    suspend fun merge(): Boolean =
        withSession { token, userId ->
            if (!isServerOnline.value) return@withSession false

            val serverTasks = taskApi.getTasks(token)
            val localTasks = taskDao.getAll(userId)

            if (serverTasks.isEmpty() && localTasks.isEmpty())
                return@withSession true

            val mergedTasks = (localTasks + serverTasks)
                .distinctBy { it.id }

            taskDao.replaceAll(userId, mergedTasks)

            taskApi.replaceTasks(token, mergedTasks)
        } ?: false

}
