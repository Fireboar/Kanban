package ch.hslu.kanban.data.local.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ch.hslu.kanban.ContextHolder

actual suspend fun provideDbDriver(
    schema: SqlSchema<QueryResult.AsyncValue<Unit>>
): SqlDriver {
    return AndroidSqliteDriver(
        schema.synchronous(),
        ContextHolder.appContext,
        "database.db"
    )
}

