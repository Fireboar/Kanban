package ch.hslu.kanban.domain.repository

import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.data.remote.api.TaskApi
import ch.hslu.kanban.domain.entity.Task
import ch.hslu.kanban.network.SyncService
import kotlin.text.insert

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskApi: TaskApi,
    private val syncService: SyncService
) {
    val isServerOnline: Boolean
        get() = syncService.isServerOnline.value

    suspend fun addTask(task: Task): Boolean {
        val newTask = taskDao.insert(task.copy())

        return isServerOnline && taskApi.addTask(newTask)
    }

    suspend fun getLocalTasks(): List<Task> =
        taskDao.getAll()

    suspend fun updateTask(task: Task): Boolean {
        taskDao.update(task)

        return isServerOnline && taskApi.updateTask(task)
    }

    suspend fun deleteTask(task: Task): Boolean {
        taskDao.delete(
            taskId = task.id
        )
        return isServerOnline && taskApi.deleteTask(task.id)
    }
}