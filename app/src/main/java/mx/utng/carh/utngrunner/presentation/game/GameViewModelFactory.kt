package mx.utng.carh.utngrunner.presentation.game

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.health.services.client.HealthServices
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.carh.utngrunner.presentation.data.datasource.PreferencesDataSource
import mx.utng.carh.utngrunner.presentation.data.health.HeartRateDataSource
import mx.utng.carh.utngrunner.presentation.data.repository.ScoreRepositoryImpl
import mx.utng.carh.utngrunner.presentation.domain.usecase.GetHighScoreUseCase
import mx.utng.carh.utngrunner.presentation.domain.usecase.SaveHighScoreUseCase

import mx.utng.carh.utngrunner.presentation.theme.UTNGRunnerTheme

class GameViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Construir el grafo de dependencias de forma explícita
        val healthClient = HealthServices.getClient(context)
        val heartRateDs = HeartRateDataSource(healthClient)
        val prefsDs = PreferencesDataSource(context)
        val repository = ScoreRepositoryImpl(prefsDs)
        return GameViewModel(
            getHighScore = GetHighScoreUseCase(repository),
            saveHighScore = SaveHighScoreUseCase(repository),
            heartRateSource = heartRateDs
        ) as T
    }
}
// En GameActivity.kt — usar la factory
class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(applicationContext)
            )
            UTNGRunnerTheme { GameScreen(viewModel = viewModel) }
        }
    }
}
