package ch.hslu.kanban.view.task.kanBanScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.kanban.model.toLocalDateTimeOrNull
import ch.hslu.kanban.viewmodel.TaskViewModel

val statuses = listOf("To Do", "In Progress", "Done")


@Composable
fun KanbanScreen(
    taskViewModel: TaskViewModel,
    paddingValues: PaddingValues
) {
    val tasks by taskViewModel.tasks.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        val screenWidth = maxWidth
        val isWideScreen = screenWidth >= 900.dp
        val columnWidth = if (isWideScreen) 0.dp else 300.dp

        val rowModifier = if (isWideScreen) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .horizontalScroll(rememberScrollState())
        }

        Row(
            modifier = rowModifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            statuses.forEach { status ->

                val verticalScroll = rememberScrollState()

                val columnModifier = if (isWideScreen) {
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                } else {
                    Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                }

                Column(
                    modifier = columnModifier
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val headerColor = when (status) {
                        "To Do" -> Color(0xFF90CAF9)
                        "In Progress" -> Color(0xFFFFF9C4)
                        "Done" -> Color(0xFFC8E6C9)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Text(
                        text = status,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            .padding(4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = tasks
                                .filter { it.status == status }
                                .sortedBy { it.toLocalDateTimeOrNull() }
                        ) { task ->
                            DraggableTaskItem(
                                task = task,
                                columnWidthDp = columnWidth.takeIf { !isWideScreen } ?: 300.dp,
                                onDelete = { taskViewModel.deleteTask(task) },
                                onMove = {
                                        targetStatus -> taskViewModel.moveTask(task, targetStatus)
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}

