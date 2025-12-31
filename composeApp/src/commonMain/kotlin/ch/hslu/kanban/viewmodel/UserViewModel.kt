package ch.hslu.kanban.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.kanban.cache.TokenStorage
import ch.hslu.kanban.cache.UserStorage
import ch.hslu.kanban.domain.entity.serverRequests.UserSimple
import ch.hslu.kanban.domain.repository.UserRepository
import ch.hslu.kanban.network.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val syncViewModel: SyncViewModel,
    private val userStorage: UserStorage,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(tokenStorage.loadToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser= MutableStateFlow(
        userStorage.loadUser()
    )
    val currentUser: StateFlow<UserSimple?> = _currentUser

    private val _allUsers = MutableStateFlow<List<UserSimple>>(emptyList())
    val allUsers: StateFlow<List<UserSimple>> = _allUsers

    private val _selectedUser= MutableStateFlow(userStorage.loadUser())
    val selectedUser: StateFlow<UserSimple?> = _selectedUser

    init {
        loadAllUsers()
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _allUsers.value = userRepository.getAllUsers()
        }
    }

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            _selectedUser.value = userRepository.getUser(userId)
        }
    }


    fun addUser(username: String, password: String, role: String) {
        viewModelScope.launch {
            val success = userRepository.addUser(username, password, role)
            if (success) {
                loadAllUsers()
                setSyncMessage(
                    "User $username erfolgreich hinzugefügt.",
                    true)
            } else {
                setSyncMessage(
                    "User konnte nicht erstellt werden.",
                    false)
            }
        }
    }

    fun updateUsername(userId: Long, newUsername: String) {
        viewModelScope.launch {
            val response = userRepository.updateUsername(userId, newUsername)

            if (response?.isSuccessful == true) {
                loadAllUsers()
                loadUser(userId)

                // Falls der eigene Benutzer geändert wurde, lokalen State aktualisieren
                if (userId == authService.currentUser?.userId) {
                    _currentUser.value = authService.currentUser
                }

                setSyncMessage(
                    "Username erfolgreich geändert.",
                    true
                )
            } else {
                setSyncMessage(
                    "Username konnte nicht geändert werden.",
                    false
                )
            }
        }
    }

    fun updatePassword(userId: Long, oldPassword: String?, newPassword: String) {
        viewModelScope.launch {
            val success = userRepository.updatePassword(
                userId,
                oldPassword,
                newPassword
            )
            if (success) {
                loadAllUsers()
                setSyncMessage(
                    "Passwort erfolgreich geändert.",
                    true)
            } else {
                setSyncMessage(
                    "Passwort konnte nicht geändert werden.",
                    false)
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            val success = userRepository.deleteUser(userId)
            if (success) {
                loadAllUsers()
                setSyncMessage("User erfolgreich gelöscht.", true)
            } else {
                setSyncMessage("User konnte nicht gelöscht werden.", false)
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val success = authService.login(username, password)
            _isLoggedIn.value = success
            _currentUser.value = if (success) authService.currentUser else null
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
            _isLoggedIn.value = false
            _currentUser.value = null
        }
    }

    fun setSyncMessage(message: String, positive: Boolean, priority: Int = 2) {
        viewModelScope.launch {
            syncViewModel.setSyncMessage(message, positive, priority)
        }
    }

    fun manualOfflineLogin(username: String) {
        viewModelScope.launch {

            val offlineUser = UserSimple(
                userId = -1L,
                userName = username,
                role = "OFFLINE"
            )

            userStorage.saveUser(offlineUser)

            _currentUser.value = offlineUser
            _isLoggedIn.value = true

            setSyncMessage("Offline-Modus aktiv", true)
        }
    }
}

