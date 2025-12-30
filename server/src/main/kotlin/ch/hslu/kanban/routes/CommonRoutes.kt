package ch.hslu.kanban.routes

import ch.hslu.kanban.data.local.database.TaskDao
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.commonRoutes(taskDao: TaskDao) {
    routing {
        // HEALTH
        get("/health") {
            call.respondText("OK")
        }
    }
}