package ch.hslu.kanban.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ch.hslu.kanban.view.bars.BottomNavigationBar
import ch.hslu.kanban.view.bars.SuccessMessage
import ch.hslu.kanban.view.bars.SuccessMessageOverlay
import ch.hslu.kanban.view.bars.TopBar
import ch.hslu.kanban.view.task.addTaskScreen.AddTaskScreen
import ch.hslu.kanban.view.task.kanBanScreen.KanbanScreen
import ch.hslu.kanban.view.task.taskDetailScreen.TaskDetailScreen
import ch.hslu.kanban.view.user.userScreen.UserScreen
import ch.hslu.kanban.viewmodel.SyncViewModel
import ch.hslu.kanban.viewmodel.TaskViewModel

enum class ScreenType { KANBAN, ADDTASK, TASKDETAIL, USER}

@Composable
fun Navigation(taskViewModel: TaskViewModel, syncViewModel: SyncViewModel) {
    var currentScreen by rememberSaveable {
        mutableStateOf(ScreenType.KANBAN)
    }

    var currentTaskId by rememberSaveable { mutableStateOf<Long?>(null) }

    fun navigateTo(screen: ScreenType, taskId: Long? = null) {
        currentScreen = screen
        currentTaskId = taskId
    }

    Scaffold(
        topBar = {
            val screenTitle = when (currentScreen) {
                ScreenType.KANBAN -> ""
                ScreenType.ADDTASK -> "Aufgabe hinzufügen"
                ScreenType.TASKDETAIL -> "Task-Details"
                ScreenType.USER -> "User"
            }
            if(currentScreen != ScreenType.KANBAN){
                TopBar(screenTitle)
            }
        },
        bottomBar = {
            Column (Modifier.fillMaxWidth()) {
                SuccessMessage(syncViewModel)
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        navigateTo(screen)
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                ScreenType.KANBAN -> KanbanScreen(
                    taskViewModel = taskViewModel,
                    onTaskClick = {
                        task -> navigateTo(ScreenType.TASKDETAIL,
                        task.id)
                    }
                )
                ScreenType.ADDTASK -> AddTaskScreen(
                    taskViewModel = taskViewModel
                )
                ScreenType.TASKDETAIL -> currentTaskId?.let { taskId ->
                    TaskDetailScreen(
                        taskId = taskId,
                        taskViewModel = taskViewModel,
                        onNavigateBack = { navigateTo(ScreenType.KANBAN) }
                    )
                }
                ScreenType.USER ->
                    UserScreen(
                        taskViewModel,
                        syncViewModel
                    )
            }
        }

    }
}


