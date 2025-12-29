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
    suspend fun getAll(): List<Task> =
        taskQueries.selectAllTasks( TaskMapper::map)
            .awaitAsList()

    suspend fun getById(taskId: Long): Task? =
        taskQueries.selectTaskById(taskId, TaskMapper::map)
            .awaitAsOneOrNull()

    suspend fun insert(task: Task): Task =
        taskQueries.transactionWithResult {
            taskQueries.insertTask(
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                status = task.status
            )

            val newId = commonQueries.lastInsertRowId().awaitAsOne()

            taskQueries.selectTaskById(newId,TaskMapper::map)
                .awaitAsOne()
        }

    suspend fun update(task: Task) =
        taskQueries.updateTask(
            id = task.id,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )

    suspend fun upsert(task: Task) =
        taskQueries.insertOrReplaceTask(
            id = task.id,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )

    suspend fun delete(taskId: Long) =
        taskQueries.deleteTaskById(taskId)

    suspend fun replaceAll(tasks: List<Task>) =
        taskQueries.transaction {
            taskQueries.deleteAllTasks()
            tasks.forEach { task ->
                taskQueries.insertOrReplaceTask(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    dueDate = task.dueDate,
                    dueTime = task.dueTime,
                    status = task.status
                )
            }
        }
}
