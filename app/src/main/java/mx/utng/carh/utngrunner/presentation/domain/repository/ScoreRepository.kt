package mx.utng.carh.utngrunner.presentation.domain.repository

/** Contrato: el dominio solo conoce la interfaz, nunca la implementación */
interface ScoreRepository {
    suspend fun getHighScore(): Int
    suspend fun saveHighScore(score: Int)
}
