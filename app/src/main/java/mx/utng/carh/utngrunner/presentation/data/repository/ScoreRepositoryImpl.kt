package mx.utng.carh.utngrunner.presentation.data.repository

import mx.utng.carh.utngrunner.presentation.data.datasource.PreferencesDataSource
import mx.utng.carh.utngrunner.presentation.domain.repository.ScoreRepository

/** Implementación concreta — la capa de datos implementa la interfaz del dominio */
class ScoreRepositoryImpl(
    private val dataSource: PreferencesDataSource
) : ScoreRepository {

    override suspend fun getHighScore(): Int =         dataSource.getHighScore()

    override suspend fun saveHighScore(score: Int) =
        dataSource.saveHighScore(score) }
