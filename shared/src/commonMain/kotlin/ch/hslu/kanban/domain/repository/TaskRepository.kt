package ch.hslu.kanban.domain.repository

import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.data.remote.api.TaskApi
import ch.hslu.kanban.domain.entity.Task
import ch.hslu.kanban.network.AuthService
import ch.hslu.kanban.network.SyncService
import kotlin.text.insert

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskApi: TaskApi,
    private val syncService: SyncService,
    private val authService: AuthService
) {
    val isServerOnline: Boolean
        get() = syncService.isServerOnline.value

    suspend fun addTask(task: Task): Boolean =
        authService.withSession { token, userId ->
            val newTask = taskDao.insert(task.copy(userId = userId))
            isServerOnline && taskApi.addTask(token, newTask)
        } ?: false


    suspend fun getLocalTasks(): List<Task> =
        authService.withSession { _, userId ->
            taskDao.getAll(userId)
        } ?: emptyList()

    suspend fun updateTask(task: Task): Boolean =
        authService.withSession { token, userId ->
            val taskToUpdate = task.copy(userId = userId)
            taskDao.update(taskToUpdate)
            isServerOnline && taskApi.updateTask(token, taskToUpdate)
        } ?: false

    suspend fun deleteTask(task: Task): Boolean =
        authService.withSession { token, userId ->
            taskDao.delete(task.id, userId)
            isServerOnline && taskApi.deleteTask(token, task.id)
        } ?: false

}