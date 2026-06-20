package mx.utng.carh.utngrunner.presentation.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import mx.utng.carh.utngrunner.presentation.domain.model.GamePhase

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory),
) {
    val state by viewModel.state.collectAsState()
    var frame by remember { mutableLongStateOf(0L) }

    // Loop de renderizado vinculado al frame del sistema
    LaunchedEffect(state.phase) {
        while (state.phase == GamePhase.PLAYING) {
            delay(16L)
            frame++
        }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onRotaryScrollEvent { event ->
                // Corona del reloj = saltar (giro hacia arriba)
                if (event.verticalScrollPixels < 0) {
                    viewModel.onJump()
                } else {
                    viewModel.onSlide()
                }
                true
            }
            .clickable { viewModel.onJump() }
    ) {
        // Canvas principal
        Canvas(modifier = Modifier.fillMaxSize()) {
            GameRenderer.draw(
                canvas = drawContext.canvas.nativeCanvas,
                width = size.width,
                height = size.height,
                state = state,
                frame = frame
            )
        }

        // Overlays de estado
        when (state.phase) {
            GamePhase.IDLE -> IdleOverlay(onStart = viewModel::onJump)
            GamePhase.DEAD -> GameOverOverlay(
                score = state.score,
                highScore = state.highScore,
                onRetry = viewModel::onJump
            )
            else -> Unit
        }
    }
}

@Composable
private fun IdleOverlay(onStart: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xBB000000))
            .clickable(onClick = onStart),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "UTNG Runner",
                style = MaterialTheme.typography.title3,
                color = Color(0xFFF9A825),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Toca o gira la corona",
                style = MaterialTheme.typography.body2,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun GameOverOverlay(score: Int, highScore: Int, onRetry: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xBB000000))
            .clickable(onClick = onRetry),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "¡HAS CAÍDO!",
                style = MaterialTheme.typography.title3,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Puntos: $score",
                style = MaterialTheme.typography.body1,
                color = Color.White
            )
            Text(
                "Récord: $highScore",
                style = MaterialTheme.typography.body2,
                color = Color.Yellow
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Toca para reintentar",
                style = MaterialTheme.typography.caption1,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
