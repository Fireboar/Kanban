package ch.hslu.kanban.domain.entity

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = "",
    val dueDate: String,
    val dueTime: String,
    val status: String? = "To Do"
)