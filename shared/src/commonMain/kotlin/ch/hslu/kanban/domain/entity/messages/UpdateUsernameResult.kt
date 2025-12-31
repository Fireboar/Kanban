package ch.hslu.kanban.domain.entity.messages

data class UpdateUsernameResult(
    val isSuccessful: Boolean,
    val token: String?
)