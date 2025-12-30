package ch.hslu.kanban.security

import ch.hslu.kanban.domain.entity.User
import java.security.MessageDigest
import java.security.SecureRandom


class PasswordService {

    fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Invalid hex string" }
        return hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun verifyPassword(password: String, user: User): Boolean {
        val saltBytes = hexToBytes(user.salt)
        val hashAttempt = hashPasswordWithSalt(password, saltBytes)
        return hashAttempt == user.passwordHash
    }

    fun hashPasswordWithSalt(password: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val saltedPassword = passwordBytes + salt
        val hash = digest.digest(saltedPassword)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun generateSalt(length: Int = 16): ByteArray {
        val salt = ByteArray(length)
        SecureRandom().nextBytes(salt)
        return salt
    }

}
