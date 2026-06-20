package mx.utng.carh.utngrunner.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import androidx.health.services.client.HealthServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.carh.utngrunner.presentation.data.datasource.PreferencesDataSource
import mx.utng.carh.utngrunner.presentation.data.health.HeartRateDataSource
import mx.utng.carh.utngrunner.presentation.data.repository.ScoreRepositoryImpl
import mx.utng.carh.utngrunner.presentation.domain.model.GamePhase
import mx.utng.carh.utngrunner.presentation.domain.model.GameState
import mx.utng.carh.utngrunner.presentation.domain.model.Player as GamePlayer
import mx.utng.carh.utngrunner.presentation.domain.usecase.GetHighScoreUseCase
import mx.utng.carh.utngrunner.presentation.domain.usecase.SaveHighScoreUseCase

enum class HapticType { JUMP, HIT }

class GameViewModel(
    private val getHighScore: GetHighScoreUseCase,
    private val saveHighScore: SaveHighScoreUseCase,
    private val heartRateSource: HeartRateDataSource
) : ViewModel() {

    // Factory para instanciar sin Hilt/Koin
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val context = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val prefs = PreferencesDataSource(context)
                val repo = ScoreRepositoryImpl(prefs)
                val healthClient = HealthServices.getClient(context)
                val hrSource = HeartRateDataSource(healthClient)
                
                return GameViewModel(
                    GetHighScoreUseCase(repo),
                    SaveHighScoreUseCase(repo),
                    hrSource
                ) as T
            }
        }
    }
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()
    
    private val _hapticChannel = Channel<HapticType>(Channel.CONFLATED)
    val hapticEvents = _hapticChannel.receiveAsFlow()

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
                _state.update { 
                    val next = GameEngine.update(it, gameFrame++)
                    if (next.lives < it.lives) {
                        _hapticChannel.trySend(HapticType.HIT)
                    }
                    next
                }
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
                    _hapticChannel.trySend(HapticType.JUMP)
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
