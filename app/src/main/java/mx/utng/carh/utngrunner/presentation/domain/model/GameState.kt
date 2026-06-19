package mx.utng.carh.utngrunner.presentation.domain.model

import androidx.media3.common.Player

/** Estado inmutable del juego — Unidireccional Data Flow */
data class GameState(
    val phase: GamePhase = GamePhase.IDLE,
    val score: Int = 0,
    val level: Int = 1,
    val lives: Int = 3,
    val highScore: Int = 0,
    val player: Player = Player(),
    val obstacles: List<Obstacle> = emptyList(),
    val coins: List<Coin> = emptyList(),
    val heartRate: Int = 72,
    val gameSpeed: Float = 3f
)
enum class GamePhase { IDLE, PLAYING, PAUSED, DEAD }