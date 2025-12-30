package ch.hslu.kanban

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.hslu.kanban.data.local.database.AppDatabase
import ch.hslu.kanban.data.local.database.DatabaseProvider
import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.domain.entity.Task
import ch.hslu.kanban.routes.commonRoutes
import ch.hslu.kanban.routes.taskRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json

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
    }

    // DAOs
    val taskDao = initTaskDao()

    // Beispiel Tasks
    seedTasks(taskDao)

    // TaskRoutes
    taskRoutes(taskDao)

    // CommonRoutes
    commonRoutes(taskDao)
}

suspend fun seedTasks(taskDao: TaskDao) {
    val tasks = taskDao.getAll()
    if (tasks.isEmpty()) {
        val defaultTask = Task(
            id = 0, // DB setzt Auto-Increment
            title = "Server-Task",
            description = "Dies ist ein Default-Task",
            dueDate = "12.12.2025",
            dueTime = "12:00",
            status = "To Do"
        )
        taskDao.insert(defaultTask)
    }
}

suspend fun initTaskDao(): TaskDao {
    val driver = JdbcSqliteDriver("jdbc:sqlite:server.db")
    AppDatabase.Schema.create(driver).await()

    val databaseProvider = DatabaseProvider(driver)
    return TaskDao(
        databaseProvider.tasksQueries,
        databaseProvider.commonQueries
    )
}