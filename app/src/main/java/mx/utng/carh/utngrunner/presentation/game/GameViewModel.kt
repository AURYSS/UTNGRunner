package mx.utng.carh.utngrunner.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.carh.utngrunner.presentation.data.health.HeartRateDataSource
import mx.utng.carh.utngrunner.presentation.domain.model.GamePhase
import mx.utng.carh.utngrunner.presentation.domain.model.GameState
import mx.utng.carh.utngrunner.presentation.domain.model.Player as GamePlayer
import mx.utng.carh.utngrunner.presentation.domain.usecase.GetHighScoreUseCase
import mx.utng.carh.utngrunner.presentation.domain.usecase.SaveHighScoreUseCase

class GameViewModel(
    private val getHighScore: GetHighScoreUseCase,
    private val saveHighScore: SaveHighScoreUseCase,
    private val heartRateSource: HeartRateDataSource
) : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()
    private var gameFrame = 0L
    private var gameJob: Job? = null

    init {
        loadHighScore()
        observeHeartRate()
    }

    fun startGame() {
        gameJob?.cancel()
        _state.value = GameState(
            phase = GamePhase.PLAYING,
            highScore = _state.value.highScore
        )
        gameFrame = 0L
        gameJob = viewModelScope.launch {
            // 60 fps → ~16ms por frame
            while (_state.value.phase == GamePhase.PLAYING) {
                delay(16L)
                _state.update { GameEngine.update(it, gameFrame++) }
            }
            if (_state.value.phase == GamePhase.DEAD) {
                saveHighScore(_state.value.score)
                loadHighScore() // Recargar para actualizar el visual
            }
        }
    }

    fun onJump() {
        val s = _state.value
        when (s.phase) {
            GamePhase.IDLE, GamePhase.DEAD -> startGame()
            GamePhase.PLAYING -> {
                if (!s.player.isJumping && s.player.y >= GamePlayer.FLOOR_Y - 5f) {
                    _state.update { it.copy(player = it.player.copy(                         
                        velocityY = GamePlayer.JUMP_VELOCITY, isJumping = true
                    ))}
                }
            }
            else -> {}
        }
    }

    fun onSlide() {
        if (_state.value.phase != GamePhase.PLAYING || _state.value.player.isJumping) return
        _state.update { it.copy(player = it.player.copy(
            slideFrames = GamePlayer.SLIDE_DURATION
        ))}
    }

    private fun loadHighScore() {
        viewModelScope.launch {
            val hs = getHighScore()
            _state.update { it.copy(highScore = hs) }
        }
    }

    private fun observeHeartRate() {
        viewModelScope.launch {
            heartRateSource.heartRate.collect { bpm ->
                _state.update { it.copy(heartRate = bpm) }
            }
        }
        heartRateSource.startMonitoring()
    }

    override fun onCleared() {
        gameJob?.cancel()
    }
}
