package ch.hslu.kanban.routes

import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.domain.entity.Task
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

fun Application.taskRoutes(taskDao: TaskDao) {
    routing {
        // CREATE
        post("/tasks") {
            val task = call.receive<Task>()

            // Upsert in Datenbank
            val insertedTask = taskDao.upsert(task)

            // Response
            call.respond(HttpStatusCode.OK, insertedTask)
        }

        // READ ALL
        get("/tasks") {
            // Datenbank lesen
            val tasks = taskDao.getAll()

            // Response
            call.respond(tasks)
        }

        // UPDATE
        put("/tasks/{id}") {
            // ID aus URL
            val taskId = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid task ID")

            // Task aus Request Body
            val updatedTaskData = call.receive<Task>()

            // Existenz prüfen
            val existingTask = taskDao.getById(taskId)
            if (existingTask == null) {
                return@put call.respond(
                    HttpStatusCode.NotFound,
                    "Task with id=$taskId not found")
            }

            // ID aus URL setzen
            val taskToUpdate = updatedTaskData.copy(
                id = taskId
            )

            // Update in DB
            taskDao.update(taskToUpdate)

            // Response
            call.respond(HttpStatusCode.OK, taskToUpdate)
        }

        // DELETE
        delete("/tasks/{id}") {
            // ID aus URL
            val taskId = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid task ID")

            // Existenz prüfen
            val existingTask = taskDao.getById(taskId)
            if (existingTask == null) {
                return@delete call.respond(
                    HttpStatusCode.NotFound,
                    "Task not found")
            }

            // Task löschen
            taskDao.delete(
                taskId = existingTask.id
            )

            // Response
            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Deleted task $taskId"))
        }



        // REPLACE
        post("/tasks/replace") {
            // Tasks aus Request Body
            val tasks = call.receive<List<Task>>()

            // Dao Replace
            taskDao.replaceAll(tasks)

            // Response
            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Tasks replaced successfully"))
        }
    }
}