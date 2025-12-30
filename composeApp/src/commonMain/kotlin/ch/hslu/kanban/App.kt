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
import ch.hslu.kanban.data.remote.api.TaskApi
import ch.hslu.kanban.data.remote.api.UserApi
import ch.hslu.kanban.domain.cache.TokenStorage
import ch.hslu.kanban.domain.cache.UserStorage
import ch.hslu.kanban.domain.repository.TaskRepository
import ch.hslu.kanban.domain.repository.UserRepository
import ch.hslu.kanban.network.AuthService
import ch.hslu.kanban.network.SyncService
import ch.hslu.kanban.view.Navigation
import ch.hslu.kanban.viewmodel.SyncViewModel
import ch.hslu.kanban.viewmodel.TaskViewModel
import ch.hslu.kanban.viewmodel.UserViewModel
import ch.hslu.newcmpproject.data.remote.api.AuthApi

@Composable
fun App() {
    var taskViewModel by remember { mutableStateOf<TaskViewModel?>(null) }
    var userViewModel by remember { mutableStateOf<UserViewModel?>(null) }
    var syncViewModel by remember { mutableStateOf<SyncViewModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Database
        val driver = provideDbDriver(AppDatabase.Schema)
        val database = DatabaseProvider(driver)

        // Dao
        val taskDao = TaskDao(
            database.tasksQueries,
            database.commonQueries
        )

        // API
        val taskApi = TaskApi()
        val userApi = UserApi()


        // Storage-Instanzen erstellen
        val tokenStorage = TokenStorage()
        val userStorage = UserStorage()
        val authApi = AuthApi()


        // AuthService
        val authService = AuthService(
            tokenStorage = tokenStorage,
            userStorage = userStorage,
            authApi = authApi
        )

        // SyncService
        val syncService = SyncService(
            taskApi,
            taskDao,
            authService
        )

        // Repositories
        val taskRepository = TaskRepository(
            taskDao, taskApi, authService,
            syncService
        )
        val userRepository = UserRepository(
            userApi,
            authService
        )

        // ViewModels
        syncViewModel = SyncViewModel(syncService)

        userViewModel = UserViewModel(
            authService, userRepository, syncViewModel!!,
            userStorage = userStorage,
            tokenStorage = tokenStorage
        )

        taskViewModel = TaskViewModel(
            taskRepository,
            syncViewModel = syncViewModel!!,
            syncService = syncService
        )

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
                taskViewModel = taskViewModel!!,
                userViewModel = userViewModel!!,
                syncViewModel = syncViewModel!!
            )
        }
    }
}




