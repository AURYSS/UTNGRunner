package mx.utng.carh.utngrunner.presentation.game

import mx.utng.carh.utngrunner.presentation.domain.model.Coin
import mx.utng.carh.utngrunner.presentation.domain.model.GameState
import mx.utng.carh.utngrunner.presentation.domain.model.GamePhase
import mx.utng.carh.utngrunner.presentation.domain.model.Obstacle
import mx.utng.carh.utngrunner.presentation.domain.model.ObstacleType
import mx.utng.carh.utngrunner.presentation.domain.model.Player as GamePlayer
import kotlin.math.hypot

object GameEngine {
    fun update(state: GameState, frame: Long): GameState {
        if (state.phase != GamePhase.PLAYING) return state
        val updatedPlayer = updatePlayer(state.player)
        val newScore = state.score + 1
        val newLevel = calculateLevel(newScore)
        val newSpeed = 3f + (newLevel * 0.6f)
        val updatedObs = updateObstacles(state.obstacles, newSpeed, frame)
        val updatedCoins = updateCoins(state.coins, newSpeed, frame)
        val afterCollision = checkCollisions(
            updatedPlayer, updatedObs, updatedCoins, state.lives
        )
        return state.copy(
            player = afterCollision.player,
            score = newScore,
            level = newLevel,
            lives = afterCollision.lives,
            gameSpeed = newSpeed,
            obstacles = afterCollision.obstacles,
            coins = afterCollision.coins,
            phase = if (afterCollision.lives <= 0) GamePhase.DEAD
            else GamePhase.PLAYING
        )
    }

    private fun updatePlayer(p: GamePlayer): GamePlayer {
        val newVelY = p.velocityY + GamePlayer.GRAVITY
        val newY = (p.y + newVelY).coerceAtMost(GamePlayer.FLOOR_Y)
        val landed = newY >= GamePlayer.FLOOR_Y
        return p.copy(
            y = newY,
            velocityY = if (landed) 0f else newVelY,
            isJumping = !landed && p.isJumping,
            isSliding = p.slideFrames > 0,
            slideFrames = (p.slideFrames - 1).coerceAtLeast(0),
            isInvincible = p.invincibleFrames > 0,
            invincibleFrames = (p.invincibleFrames - 1).coerceAtLeast(0)
        )
    }

    private fun calculateLevel(score: Int): Int =
        (1 + score / 300).coerceAtMost(5)

    private fun updateObstacles(
        obstacles: List<Obstacle>, speed: Float, frame: Long
    ): List<Obstacle> {
        val moved = obstacles
            .map { it.copy(x = it.x - speed) }
            .filter { it.x > -50f }
        
        // Spawn probabilístico (cada ~1 segundo a 60fps)
        return if (frame % 60 == 0L && Math.random() < 0.6) {
            val type = ObstacleType.entries.random()
            moved + Obstacle(x = 240f, width = type.w, height = type.h, type = type)
        } else moved
    }

    private fun updateCoins(coins: List<Coin>, speed: Float, frame: Long): List<Coin> {
        val moved = coins
            .filter { !it.collected && it.x > -50f }
            .map { it.copy(x = it.x - speed, phase = it.phase + 0.1f) }

        // Spawn de monedas
        return if (frame % 80 == 0L && Math.random() < 0.4) {
            moved + Coin(x = 240f, y = GamePlayer.FLOOR_Y - (40..100).random().toFloat())
        } else moved
    }

    /** Detección AABB (Axis-Aligned Bounding Box) */
    private fun checkCollisions(
        player: GamePlayer, obstacles: List<Obstacle>, coins: List<Coin>, currentLives: Int
    ): CollisionResult {
        val floorOffset = GamePlayer.FLOOR_Y + 20f
        val cLeft = player.x - 10f
        val cRight = player.x + 18f
        val cTop = player.y - (if (player.isSliding) 8f else 30f)
        val cBot = player.y + 20f

        val hitObs = obstacles.filter { o ->
            !player.isInvincible &&
                    cRight > o.x + 4f && cLeft < o.x + o.width - 4f &&
                    cBot > floorOffset - o.height && cTop < floorOffset
        }

        val updatedCoins = coins.map { cn ->
            val dist = hypot(player.x - cn.x, player.y - cn.y)
            if (!cn.collected && dist < 22.0) cn.copy(collected = true) else cn
        }

        return CollisionResult(
            player = if (hitObs.isNotEmpty()) player.copy(invincibleFrames = GamePlayer.INVINCIBLE_FRAMES) else player,
            lives = if (hitObs.isNotEmpty()) (currentLives - 1).coerceAtLeast(0) else currentLives,
            obstacles = obstacles.map { if (it in hitObs) it.copy(x = -999f) else it },
            coins = updatedCoins
        )
    }
}

data class CollisionResult(
    val player: GamePlayer,
    val lives: Int,
    val obstacles: List<Obstacle>,
    val coins: List<Coin>
)
