package ch.hslu.kanban.data.local.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import ch.hslu.kanban.data.local.database.mapper.TaskMapper
import ch.hslu.kanban.domain.entity.Task

class TaskDao(
    private val taskQueries: TasksQueries,
    private val commonQueries: CommonQueries
) {
    suspend fun getAll(userId: Long): List<Task> =
        taskQueries.selectAllTasks(userId, TaskMapper::map)
            .awaitAsList()

    suspend fun getById(userId: Long, taskId: Long): Task? =
        taskQueries.selectTaskById(taskId, userId, TaskMapper::map)
            .awaitAsOneOrNull()


    suspend fun insert(task: Task): Task =
        taskQueries.transactionWithResult {
            taskQueries.insertTask(
                userId = task.userId,
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                status = task.status
            )

            val newId = commonQueries.lastInsertRowId().awaitAsOne()

            taskQueries.selectTaskById(
                newId,
                task.userId,
                TaskMapper::map)
                .awaitAsOne()
        }

    suspend fun update(task: Task) =
        taskQueries.updateTask(
            id = task.id,
            userId = task.userId,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )

    suspend fun upsert(task: Task) =
        taskQueries.insertOrReplaceTask(
            id = task.id,
            userId = task.userId,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )

    suspend fun delete(taskId: Long, userId: Long) =
        taskQueries.deleteTaskById(taskId, userId)

    suspend fun replaceAll(userId: Long, tasks: List<Task>) =
        taskQueries.transaction {
            taskQueries.deleteAllTasks(userId)
            tasks.forEach { task ->
                taskQueries.insertOrReplaceTask(
                    id = task.id,
                    userId = userId,
                    title = task.title,
                    description = task.description,
                    dueDate = task.dueDate,
                    dueTime = task.dueTime,
                    status = task.status
                )
            }
        }

}
