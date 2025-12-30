package ch.hslu.kanban.view.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.hslu.kanban.viewmodel.SyncViewModel
import kotlinx.coroutines.delay

@Composable
fun SuccessMessageOverlay(
    syncViewModel: SyncViewModel,
    modifier: Modifier = Modifier,
    autoHideMillis: Long = 3_000
) {
    val syncMessage by syncViewModel.syncMessage.collectAsState()
    val serverOnline by syncViewModel.isServerOnline.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val visible = syncMessage.text.isNotEmpty()

    // Auto-Hide
    LaunchedEffect(syncMessage.text) {
        if (visible) {
            expanded = false
            delay(autoHideMillis)

        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it }
        ) + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // Message
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (syncMessage.isPositive)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFF44336)
                    )
                    .clickable { expanded = !expanded }
                    .padding(12.dp)
            ) {
                Text(
                    text = syncMessage.text,
                    color = Color.White,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Serverstatus
            Box(
                modifier = Modifier
                    .background(
                        if (serverOnline)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFF44336)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = if (serverOnline) "Server online" else "Server offline",
                    color = Color.White
                )
            }
        }
    }
}
