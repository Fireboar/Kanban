package ch.hslu.kanban.routes

import ch.hslu.kanban.data.local.database.UserDao
import ch.hslu.kanban.domain.entity.serverRequests.CreateUserRequest
import ch.hslu.kanban.domain.entity.serverRequests.UpdatePasswordRequest
import ch.hslu.kanban.domain.entity.serverRequests.UpdateUsernameRequest
import ch.hslu.kanban.domain.entity.serverRequests.UserSimple
import ch.hslu.kanban.security.JwtConfig
import ch.hslu.kanban.security.PasswordService
import ch.hslu.kanban.security.isAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

fun Application.userRoutes(userDao: UserDao, passwordService: PasswordService) {

    routing {

        // Read All
        get("/users") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            if (!principal.isAdmin()) {
                return@get call.respond(HttpStatusCode.Forbidden, "Admin only")
            }

            val users = userDao.getAll()
            call.respond(users.map { user ->
                UserSimple(
                    userId = user.id,
                    userName = user.username,
                    role = user.role
                )
            })
        }

        // Read Single
        get("/users/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val requesterId = principal.payload.getClaim("userId").asLong()
            val requesterRole = principal.payload.getClaim("role").asString()

            // ID aus der URL lesen
            val userId = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid user id")

            // Zugriff prüfen: entweder Admin oder eigener User
            if (requesterRole != "ADMIN" && requesterId != userId) {
                return@get call.respond(
                    HttpStatusCode.Forbidden,
                    "Access denied")
            }

            // User aus DB holen
            val user = userDao.getById(userId)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    "User not found")

            // Nur sichere Daten zurückgeben
            call.respond(
                UserSimple(
                    userId = user.id,
                    userName = user.username,
                    role = user.role
                )
            )
        }

        // Create
        post("/users") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            if (!principal.isAdmin()) {
                return@post call.respond(
                    HttpStatusCode.Forbidden,
                    "Admin only")
            }

            val userrequest = call.receive<CreateUserRequest>()

            val existing = userDao.getByUsername(userrequest.username)
            if (existing != null) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    "User already exists")
            }

            val userId = userDao.insert(
                userrequest.username,
                userrequest.password,
                userrequest.role
            )

            val user = userDao.getById(userId)!!

            val userResponse = UserSimple(
                userId = user.id,
                userName = user.username,
                role = user.role
            )

            call.respond(userResponse)
        }

        // Update Username
        put("/user/username") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            val adminRole = principal.payload.getClaim("role").asString()
            val requesterId = principal.payload.getClaim("userId").asLong()

            val request = call.receive<UpdateUsernameRequest>()
            val targetUserId = request.userId ?: requesterId

            // Nur Admin darf andere User ändern
            if (targetUserId != requesterId && adminRole != "ADMIN") {
                return@put call.respond(
                    HttpStatusCode.Forbidden,
                    "Only admins can update other users")
            }

            // Prüfen, ob Username bereits existiert
            val existingUser = userDao.getByUsername(request.username)
            if (existingUser != null && existingUser.id != targetUserId) {
                return@put call.respond(
                    HttpStatusCode.Conflict,
                    "Username already exists")
            }

            // Update durchführen
            userDao.updateUsername(targetUserId, request.username)

            val targetUser = userDao.getById(targetUserId)!!

            // Neues JWT nur für eigenen User
            if (targetUserId == requesterId) {
                val newToken = JwtConfig.generateToken(
                    targetUser.id,
                    targetUser.username,
                    targetUser.role)
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("token" to newToken))
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "User updated"))
            }
        }

        // Update Password
        put("/user/password") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            val adminRole = principal.payload.getClaim("role").asString()
            val requesterId = principal.payload.getClaim("userId").asLong()

            val request = call.receive<UpdatePasswordRequest>()
            val targetUserId = request.userId ?: requesterId

            if (targetUserId != requesterId && adminRole != "ADMIN") {
                return@put call.respond(
                    HttpStatusCode.Forbidden,
                    "Only admins can change other users' passwords")
            }

            val user = userDao.getById(targetUserId)
                ?: return@put call.respond(
                    HttpStatusCode.NotFound,
                    "User not found")

            if (targetUserId == requesterId) {
                val oldPassword = request.oldPassword
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        "Old password required"
                    )

                if (!passwordService.verifyPassword(oldPassword, user)) {
                    return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        "Wrong password"
                    )
                }
            }

            userDao.updatePassword(
                id = targetUserId,
                newPassword = request.newPassword
            )

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Password updated"))
        }

        // Delete
        delete("/users/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            if (!principal.isAdmin()) {
                return@delete call.respond(
                    HttpStatusCode.Forbidden,
                    "Admin only")
            }

            val userId = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            userDao.delete(userId)

            call.respond(HttpStatusCode.OK)
        }
    }
}