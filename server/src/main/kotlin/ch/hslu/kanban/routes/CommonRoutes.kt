package ch.hslu.kanban.routes

import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.data.local.database.UserDao
import ch.hslu.kanban.domain.entity.serverRequests.LoginRequest
import ch.hslu.kanban.security.JwtConfig
import ch.hslu.kanban.security.PasswordService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.commonRoutes() {
    routing {
        // HEALTH
        get("/health") {
            call.respondText("OK")
        }
    }
}