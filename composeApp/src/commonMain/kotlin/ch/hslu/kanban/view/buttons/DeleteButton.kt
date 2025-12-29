package ch.hslu.kanban.view.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun DeleteButton(onDelete: () -> Unit, title: String, text: String) {
    var showDialog by remember { mutableStateOf(false) }

    // Button
    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.White)
    }

    // Bestätigungsdialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White)
                ) {
                    Text("Ja")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White)
                ) {
                    Text("Nein")
                }
            }
        )
    }
}

