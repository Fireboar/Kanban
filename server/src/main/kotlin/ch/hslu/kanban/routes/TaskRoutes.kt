package ch.hslu.kanban.routes

import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.domain.entity.Task
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

fun Application.taskRoutes(taskDao: TaskDao) {
    routing {
        authenticate("auth-jwt") {
            // Create
            post("/tasks") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.payload.getClaim("userId").asLong()

                val taskFromClient = call.receive<Task>()

                // userId erzwingen
                val task = taskFromClient.copy(userId = userId)

                val insertedTask = taskDao.upsert(task)
                call.respond(HttpStatusCode.OK, insertedTask)
            }

            // Read All
            get("/tasks") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asLong()

                val tasks = taskDao.getAll(userId = userId)
                call.respond(tasks)
            }

            // Update
            put("/tasks/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // ID aus URL
                val taskId = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid task ID")

                // Task aus Request Body
                val updatedTaskData = call.receive<Task>()

                // Existenz prüfen
                val existingTask = taskDao.getById(userId, taskId)
                if (existingTask == null) {
                    return@put call.respond(
                        HttpStatusCode.NotFound,
                        "Task with id=$taskId not found")
                }

                // userId aus JWT erzwingen & ID aus URL setzen
                val taskToUpdate = updatedTaskData.copy(
                    id = taskId,
                    userId = userId
                )

                // Update in DB
                taskDao.update(taskToUpdate)

                call.respond(HttpStatusCode.OK, taskToUpdate)
            }

            // Delete
            delete("/tasks/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(
                        HttpStatusCode.Unauthorized,
                        "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // ID aus URL prüfen
                val taskId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid task ID")

                // Existenz prüfen
                val existingTask = taskDao.getById(userId, taskId)
                if (existingTask == null) {
                    return@delete call.respond(
                        HttpStatusCode.NotFound,
                        "Task not found")
                }

                // Task löschen
                taskDao.delete(
                    taskId = existingTask.id,
                    userId = userId
                )

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Deleted task $taskId"))
            }

            // Replace
            post("/tasks/replace") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // Tasks aus Request Body
                val tasksFromClient = call.receive<List<Task>>()

                // Alle Tasks für diesen User ersetzen
                taskDao.replaceAll(userId, tasksFromClient)

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Tasks replaced successfully"))
            }

        }
    }
}