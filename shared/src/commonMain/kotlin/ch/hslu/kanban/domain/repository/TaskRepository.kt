package ch.hslu.kanban.domain.repository

import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.domain.entity.Task

class TaskRepository(
    private val taskDao: TaskDao,
) {

    suspend fun addTask(task: Task): Boolean {
        taskDao.insert(task)
        return true
    }

    suspend fun getLocalTasks(): List<Task> =
        taskDao.getAll()

    suspend fun updateTask(task: Task): Boolean {
        taskDao.update(task)
        return true
    }

    suspend fun deleteTask(task: Task): Boolean {
        taskDao.delete(task.id)
        return true
    }
}