package mx.utng.carh.utngrunner.presentation.test

import mx.utng.carh.utngrunner.presentation.domain.model.GamePhase
import mx.utng.carh.utngrunner.presentation.domain.model.GameState
import mx.utng.carh.utngrunner.presentation.domain.model.Obstacle
import mx.utng.carh.utngrunner.presentation.domain.model.ObstacleType
import mx.utng.carh.utngrunner.presentation.domain.model.Player
import mx.utng.carh.utngrunner.presentation.game.GameEngine
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Pruebas unitarias para la lógica del motor del juego.
 * Ubicado en src/test para tener acceso a las librerías de JUnit.
 */
class GameEngineTest {
    
    @Test
    fun playerFallsDueToGravity() {
        val state = GameState(
            phase = GamePhase.PLAYING,
            player = Player(y = 100f, velocityY = 0f)
        )
        val next = GameEngine.update(state, frame = 0)
        assertTrue("El jugador debería caer por la gravedad", next.player.y > 100f)
    }

    @Test
    fun scoreIncreasesEveryFrame() {
        val state = GameState(phase = GamePhase.PLAYING, score = 0)
        val next = GameEngine.update(state, frame = 0)
        assertEquals(1, next.score)
    }

    @Test
    fun levelIncreasesAtScore300() {
        val state = GameState(phase = GamePhase.PLAYING, score = 299)
        val next = GameEngine.update(state, frame = 1)
        assertEquals("El nivel debería subir a 2 cuando el score llega a 300", 2, next.level)
    }

    @Test
    fun livesDecreaseOnObstacleCollision() {
        val player = Player(y = 160f, isInvincible = false)
        // Colocamos un obstáculo justo delante del jugador
        val obstacle = Obstacle(
            x = player.x + 2f,
            width = 20, height = 35, type = ObstacleType.TAREA
        )
        val state = GameState(
            phase = GamePhase.PLAYING,
            player = player,
            obstacles = listOf(obstacle),
            lives = 3
        )
        val next = GameEngine.update(state, frame = 0)
        assertTrue("Las vidas deberían disminuir al chocar", next.lives < 3)
    }

    @Test
    fun gameOverWhenLivesReachZero() {
        val state = GameState(phase = GamePhase.PLAYING, lives = 1)
        val player = Player(y = 160f, isInvincible = false)
        val obstacle = Obstacle(
            x = player.x + 2f,
            width = 20, height = 35, type = ObstacleType.TAREA
        )
        val stateWithObs = state.copy(player = player, obstacles = listOf(obstacle))
        val next = GameEngine.update(stateWithObs, frame = 0)
        
        assertEquals(0, next.lives)
        assertEquals("El estado debería ser DEAD al perder todas las vidas", GamePhase.DEAD, next.phase)
    }
}
