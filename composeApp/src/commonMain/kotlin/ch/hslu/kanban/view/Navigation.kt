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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ch.hslu.kanban.view.user.addUserScreen.AddUserScreen
import ch.hslu.kanban.view.user.loginScreen.LoginScreen
import ch.hslu.kanban.view.user.userDetailScreen.UserDetailScreen
import ch.hslu.kanban.view.user.userScreen.UserScreen
import ch.hslu.kanban.viewmodel.SyncViewModel
import ch.hslu.kanban.viewmodel.TaskViewModel
import ch.hslu.kanban.viewmodel.UserViewModel

enum class ScreenType { KANBAN, ADDTASK,  TASKDETAIL, USER, ADDUSER, USERDETAIL, LOGIN}

@Composable
fun Navigation(taskViewModel: TaskViewModel, userViewModel: UserViewModel, syncViewModel: SyncViewModel) {

    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()

    var wasLoggedIn by remember { mutableStateOf(isLoggedIn) }

    var currentScreen by rememberSaveable {
        mutableStateOf(if (isLoggedIn) ScreenType.KANBAN else ScreenType.LOGIN)
    }

    var currentTaskId by rememberSaveable { mutableStateOf<Long?>(null) }

    var currentUserId by rememberSaveable { mutableStateOf<Long?>(null) }

    val currentUser = userViewModel.currentUser.value
    val isAdmin = currentUser?.role == "ADMIN"

    fun navigateTo(screen: ScreenType, taskId: Long? = null, userId: Long? = null) {
        currentScreen = screen
        currentTaskId = taskId
        currentUserId = userId
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && !wasLoggedIn) {
            currentScreen = ScreenType.KANBAN
        } else if (!isLoggedIn && wasLoggedIn) {
            currentScreen = ScreenType.LOGIN
            currentTaskId = null
            currentUserId = null
        }
        wasLoggedIn = isLoggedIn
    }



    Scaffold(
        topBar = {
            val screenTitle = when (currentScreen) {
                ScreenType.KANBAN -> ""
                ScreenType.ADDTASK -> "Aufgabe hinzufügen"
                ScreenType.TASKDETAIL -> "Task-Details"
                ScreenType.USER -> "User"
                ScreenType.ADDUSER -> "User hinzufügen"
                ScreenType.USERDETAIL -> "User Detail"
                ScreenType.LOGIN -> "Login"
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
                        userViewModel,
                        syncViewModel,
                        onUserClick = { userId -> navigateTo(
                            ScreenType.USERDETAIL,
                            userId = userId) },
                        isAdmin = isAdmin,
                        onAddUserClick = { navigateTo(ScreenType.ADDUSER) }
                    )

                ScreenType.ADDUSER ->
                    AddUserScreen(userViewModel)

                ScreenType.USERDETAIL -> currentUserId?.let{ userId ->
                    UserDetailScreen(
                        userViewModel,
                        userId
                    )
                }

                ScreenType.LOGIN -> LoginScreen(
                    userViewModel,
                    syncViewModel
                )
            }
        }

    }
}


