package ch.hslu.kanban

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.hslu.kanban.data.local.database.AppDatabase
import ch.hslu.kanban.data.local.database.DatabaseProvider
import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.data.local.database.UserDao
import ch.hslu.kanban.data.local.database.provideDbDriver
import ch.hslu.kanban.domain.entity.Task
import ch.hslu.kanban.routes.authRoutes
import ch.hslu.kanban.routes.commonRoutes
import ch.hslu.kanban.routes.taskRoutes
import ch.hslu.kanban.routes.userRoutes
import ch.hslu.kanban.security.JwtConfig
import ch.hslu.kanban.security.PasswordService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json
import kotlin.text.insert

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module)
        .start(wait = true)
}

suspend fun Application.module() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowCredentials = true
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asLong()
                if (userId != null) JWTPrincipal(credential.payload) else null
            }
        }
    }

    // Database
    val driver = provideDbDriver(AppDatabase.Schema)
    val database = DatabaseProvider(driver)

    // PasswordService
    val passwordService = PasswordService()

    // DAOs
    val taskDao = TaskDao(
        database.tasksQueries,
        database.commonQueries
    )

    val userDao = UserDao(
        database.usersQueries,
        database.commonQueries,
        passwordService
    )

    // Beispiel Tasks
    seedUserAndTask(userDao, taskDao)

    // Routes
    taskRoutes(taskDao)
    commonRoutes()
    authRoutes(userDao,passwordService)
    userRoutes(userDao, passwordService)
}

suspend fun seedUserAndTask(userDao:UserDao, taskDao: TaskDao) {
    // Beispiel
    // User
    val existingUser = userDao.getByUsername("admin")
    val adminId = existingUser?.id ?: userDao.insert(
        username = "admin",
        password = "123",
        role = "ADMIN"
    )

    // Task
    val tasks = taskDao.getAll(adminId)
    if (tasks.isEmpty()) {
        val defaultTask = Task(
            id = 0,
            userId = adminId,
            title = "Server-Task",
            description = "Dies ist ein Default-Task",
            dueDate = "12.12.2025",
            dueTime = "12:00",
            status = "To Do"
        )
        taskDao.insert(defaultTask)
    }
}