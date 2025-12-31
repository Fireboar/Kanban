package ch.hslu.kanban.domain.entity.serverRequests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSimple(
    @SerialName("userId")
    val userId: Long,
    @SerialName("userName")
    val userName: String,
    @SerialName("role")
    val role: String
)

