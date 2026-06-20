package mx.utng.carh.utngrunner.presentation.domain.model

data class Player(
    val x: Float = 55f,
    val y: Float = FLOOR_Y,
    val velocityY: Float = 0f,
    val isJumping: Boolean = false,
    val isSliding: Boolean = false,
    val slideFrames: Int = 0,
    val isInvincible: Boolean = false,
    val invincibleFrames: Int = 0
) {
    companion object {
        const val FLOOR_Y = 160f
        const val JUMP_VELOCITY = -12f
        const val GRAVITY = 0.6f
        const val SLIDE_DURATION = 30
        const val INVINCIBLE_FRAMES = 80
    }
}

data class Obstacle(
    val x: Float,
    val width: Int,
    val height: Int,
    val type: ObstacleType
)

enum class ObstacleType(
    val label: String,
    val w: Int,
    val h: Int
) {
    TAREA("TAREA", 20, 35),
    EXAMEN("EXAMEN", 14, 50),
    BUG("BUG", 22, 22),
    REPO("REPO", 30, 18)
}

data class Coin(
    val x: Float,
    val y: Float,
    val phase: Float = 0f,
    val collected: Boolean = false
)