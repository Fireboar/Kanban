package ch.hslu.kanban.data.local.database

import app.cash.sqldelight.db.SqlDriver

class DatabaseProvider(driver: SqlDriver) {
    private val database = AppDatabase(driver)

    val tasksQueries get() = database.tasksQueries
    val commonQueries get() = database.commonQueries
    val usersQueries get() = database.usersQueries
}

