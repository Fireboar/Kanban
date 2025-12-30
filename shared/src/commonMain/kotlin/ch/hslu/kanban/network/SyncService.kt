package ch.hslu.kanban.network

import ch.hslu.kanban.SERVER_IP
import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.data.remote.api.ApiClient
import ch.hslu.kanban.data.remote.api.TaskApi
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SyncService(
    private val taskApi: TaskApi,
    private val taskDao: TaskDao
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

    suspend fun updateSyncState() {
        val serverTasks = taskApi.getTasks()
        val localTasks = taskDao.getAll()
        _isInSync.value = localTasks == serverTasks
    }


    suspend fun pull(): Boolean {
        if (!isServerOnline.value) return false

        val serverTasks = taskApi.getTasks()
        if (serverTasks.isEmpty()) return false

        taskDao.replaceAll(serverTasks)
        return true
    }

    suspend fun push(): Boolean {
        if (!isServerOnline.value) return false

        return taskApi.replaceTasks(taskDao.getAll())
    }

    suspend fun merge(): Boolean {
        if (!isServerOnline.value) return false

        val serverTasks = taskApi.getTasks()
        val localTasks = taskDao.getAll()

        if (serverTasks.isEmpty() && localTasks.isEmpty()) return true

        val mergedTasks = (localTasks + serverTasks)
            .distinctBy { it.id }

        taskDao.replaceAll(mergedTasks)

        return taskApi.replaceTasks(mergedTasks)
    }



}
