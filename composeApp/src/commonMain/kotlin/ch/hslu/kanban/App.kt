package ch.hslu.kanban

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ch.hslu.kanban.view.Navigation
import ch.hslu.kanban.viewmodel.TaskViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val taskViewModel = TaskViewModel()

    // Beispiel-Tasks
    for (i in 1..15) {
        taskViewModel.addTask(
            title = "Task $i",
            description = "Beschreibung für Task $i",
            dueDate = "31.12.2025",
            dueTime = "18:00",
            status = "To Do")
    }

    MaterialTheme {
        Navigation(taskViewModel)
    }

}




