package ch.hslu.kanban.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.JWTPrincipal

fun JWTPrincipal.isAdmin(): Boolean =
    payload.getClaim("role").asString() == "ADMIN"

object JwtConfig {
    private const val secret = "super-secret-key"
    private const val issuer = "ch.hslu.newcmpproject"
    private const val audience = "ch.hslu.newcmpproject.audience"

    const val realm = "Access to tasks"

    fun generateToken(userId: Long, username: String, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("userName", username)
            .withClaim("role", role)
            .sign(Algorithm.HMAC256(secret))
    }

    fun verifier() = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}