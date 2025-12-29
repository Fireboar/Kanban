package ch.hslu.kanban.model

import ch.hslu.kanban.domain.entity.Task
import kotlinx.datetime.LocalDateTime

fun Task.toLocalDateTimeOrNull(): LocalDateTime? {
    return try {
        val dateParts = dueDate.split(".").map { it.toInt() }
        val timeParts = dueTime.split(":").map { it.toInt() }

        LocalDateTime(year = dateParts[2],
            month = dateParts[1],
            day = dateParts[0],
            hour = timeParts[0],
            minute = timeParts[1]
        )
    } catch (e: Exception) {
        null
    }
}

