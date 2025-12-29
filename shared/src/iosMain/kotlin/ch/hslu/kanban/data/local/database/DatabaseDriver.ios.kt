package ch.hslu.kanban.data.local.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual suspend fun provideDbDriver(
    schema: SqlSchema<QueryResult.AsyncValue<Unit>>
): SqlDriver {
    val driver: SqlDriver = NativeSqliteDriver(
        schema.synchronous(),
        "database.db",
        maxReaderConnections = 4
    )
    schema.create(driver).await()
    return driver
}

