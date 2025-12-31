package ch.hslu.kanban.data.local.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import ch.hslu.kanban.data.local.database.mapper.UserMapper
import ch.hslu.kanban.domain.entity.User
import ch.hslu.kanban.security.PasswordService

class UserDao(
    private val userQueries: UsersQueries,
    private val commonQueries: CommonQueries,
    private val passwordService : PasswordService
) {
    suspend fun getAll(): List<User> =
        userQueries.selectAllUsers(UserMapper::map).awaitAsList()

    suspend fun getById(id: Long): User? =
        userQueries.selectUserById(id, UserMapper::map).awaitAsOneOrNull()

    suspend fun getByUsername(username: String): User? =
        userQueries.selectUserByUsername(username, UserMapper::map).awaitAsOneOrNull()

    suspend fun insert(username: String, password: String, role: String): Long {
        val salt = passwordService.generateSalt()
        val hash = passwordService.hashPasswordWithSalt(password, salt)
        val user = insertSecure(
            username = username,
            passwordHash = hash,
            salt = salt,
            role= role)
        return user.id
    }

    suspend fun insertSecure(
        username: String,
        passwordHash: String,
        salt: ByteArray,
        role: String
    ): User = userQueries.transactionWithResult {
        // Salt in Hex umwandeln
        val saltHex = salt.joinToString("") { "%02x".format(it) }

        // User einfügen
        userQueries.insertUser(
            username = username,
            passwordHash = passwordHash,
            salt = saltHex,
            role = role
        )

        // Letzte ID holen
        val newId = commonQueries.lastInsertRowId().awaitAsOne()
        if (newId == 0L) {
            throw IllegalStateException("Insert failed: no ID returned.")
        }

        // User anhand der ID abrufen
        userQueries.selectUserById(newId, UserMapper::map).awaitAsOne()
    }

    suspend fun updateUsername(id: Long, username: String) =
        userQueries.updateUsername(username, id)

    suspend fun updatePassword(id: Long, newPassword:String) {
        val newSalt = passwordService.generateSalt()
        val newPasswordHash = passwordService.hashPasswordWithSalt(
            password = newPassword,
            salt = newSalt
        )
        updatePasswordSecure(
            id,
            newPasswordHash,
            newSalt.joinToString("") { "%02x".format(it) })
    }

    suspend fun updatePasswordSecure(id: Long, hash: String, salt: String) =
        userQueries.updatePassword(hash, salt, id)

    suspend fun delete(id: Long) =
        userQueries.deleteUserById(id)




}


