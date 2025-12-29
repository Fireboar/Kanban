package ch.hslu.kanban

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ch.hslu.kanban.data.local.database.AppDatabase
import ch.hslu.kanban.data.local.database.DatabaseProvider
import ch.hslu.kanban.data.local.database.TaskDao
import ch.hslu.kanban.data.local.database.provideDbDriver
import ch.hslu.kanban.domain.repository.TaskRepository
import ch.hslu.kanban.view.Navigation
import ch.hslu.kanban.viewmodel.TaskViewModel

@Composable
fun App() {
    var taskViewModel by remember { mutableStateOf<TaskViewModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val driver = provideDbDriver(AppDatabase.Schema)
        val database = DatabaseProvider(driver)

        val taskDao = TaskDao(
            database.tasksQueries,
            database.commonQueries
        )

        val taskRepository = TaskRepository(taskDao)

        taskViewModel = TaskViewModel(taskRepository)

        isLoading = false
    }


    if (!isLoading) {
        /*// Beispiel-Tasks
        for (i in 1..15) {
            taskViewModel?.addTask(
                title = "Task $i",
                description = "Beschreibung für Task $i",
                dueDate = "31.12.2025",
                dueTime = "18:00",
                status = "To Do")
        }*/

        MaterialTheme {
            Navigation(
                taskViewModel = taskViewModel!!
            )
        }
    }
}




