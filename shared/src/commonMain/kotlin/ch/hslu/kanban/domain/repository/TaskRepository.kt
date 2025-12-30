package ch.hslu.kanban.domain.repository

import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.data.remote.api.TaskApi
import ch.hslu.kanban.domain.entity.Task
import ch.hslu.kanban.domain.entity.serverRequests.Token
import ch.hslu.kanban.network.AuthService
import ch.hslu.kanban.network.SyncService
import kotlin.text.insert

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskApi: TaskApi,
    private val authService: AuthService,
    private val syncService: SyncService
) {

    private val token: Token?
        get() = authService.token

    private val userId get() = authService.currentUser?.userId

    val isServerOnline: Boolean
        get() = syncService.isServerOnline.value

    suspend fun addTask(task: Task): Boolean {
        val id = userId ?: return false
        val newTask = taskDao.insert(task.copy(userId = id))

        return isServerOnline && taskApi.addTask(token!!, newTask)
    }

    suspend fun getLocalTasks(): List<Task> =
        userId?.let { taskDao.getAll(it) } ?: emptyList()

    suspend fun updateTask(task: Task): Boolean {
        val id = userId ?: return false
        taskDao.update(task.copy(userId = id))

        return isServerOnline && taskApi.updateTask(token!!, task)
    }

    suspend fun deleteTask(task: Task): Boolean {
        val id = userId ?: return false
        taskDao.delete(
            task.id,
            id
        )
        return isServerOnline && taskApi.deleteTask(token!!, task.id)
    }


}