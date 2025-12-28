package ch.hslu.kanban

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kanban",
        state = rememberWindowState(
            width = 1600.dp,
            height = 800.dp
        )
    ) {
        App()
    }
}