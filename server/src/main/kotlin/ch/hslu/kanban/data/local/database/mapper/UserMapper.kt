package ch.hslu.kanban.data.local.database.mapper

import ch.hslu.kanban.domain.entity.User

object UserMapper {
    fun map(
        id: Long,
        username: String,
        passwordHash: String,
        salt: String,
        role: String
    ) = User(id, username, passwordHash, salt, role)
}