package ch.hslu.kanban.view.task.addTaskScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.kanban.view.task.taskForm.TaskForm
import ch.hslu.kanban.viewmodel.TaskViewModel

@Composable
fun AddTaskScreen(taskViewModel: TaskViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
        ) {
            TaskForm(
                taskViewModel = taskViewModel,
                buttonText = "Aufgabe hinzufügen",
                onSubmit = { task ->
                    taskViewModel.addTask(
                        title = task.title,
                        description = task.description,
                        dueDate = task.dueDate,
                        dueTime = task.dueTime,
                        status = task.status
                    )
                },
                onNavigateBack = { }
            )

            val tasks by taskViewModel.tasks.collectAsState()

            tasks.forEach { task ->
                Card(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE0F7FA)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        task.description?.let { Text(text = it) }
                        Text(text = task.dueDate)
                        Text(text = task.dueTime)
                    }
                }
            }


        }
    }
}

