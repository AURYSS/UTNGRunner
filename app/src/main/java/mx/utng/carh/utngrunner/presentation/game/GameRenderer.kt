package mx.utng.carh.utngrunner.presentation.game

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import mx.utng.carh.utngrunner.presentation.domain.model.Coin
import mx.utng.carh.utngrunner.presentation.domain.model.GameState
import mx.utng.carh.utngrunner.presentation.domain.model.Obstacle
import mx.utng.carh.utngrunner.presentation.domain.model.Player
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

/** GameRenderer: SOLO dibuja utilizando el Canvas nativo de Android. */
object GameRenderer {
    private val COLORS = GameColors()

    fun draw(canvas: Canvas, size: Size, state: GameState, frame: Long) {
        drawBackground(canvas, size)
        drawGround(canvas, size)
        drawCoins(canvas, state.coins, frame)
        drawObstacles(canvas, state.obstacles)
        drawPlayer(canvas, state.player, frame)
        drawHUD(canvas, size, state)
    }

    private fun drawBackground(canvas: Canvas, size: Size) {
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, size.height.toFloat(),
                COLORS.skyTop.toArgb(), COLORS.skyBottom.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size.width.toFloat(), size.height.toFloat(), paint)
    }

    private fun drawGround(canvas: Canvas, size: Size) {
        val paint = Paint().apply { color = COLORS.ground.toArgb() }
        val groundY = Player.FLOOR_Y + 20f
        canvas.drawRect(0f, groundY, size.width.toFloat(), size.height.toFloat(), paint)
    }

    private fun drawPlayer(canvas: Canvas, player: Player, frame: Long) {
        val alpha = if (player.isInvincible && (frame / 4) % 2 == 0L) 0.3f else 1f
        val yPos = player.y

        // Cuerpo
        val bodyColor = Color(0xFFE65100).copy(alpha = alpha).toArgb()
        val bodyPaint = Paint().apply { color = bodyColor }
        val bodyRect = if (player.isSliding) {
            RectF(player.x - 12f, yPos + 4f, player.x + 16f, yPos + 18f)
        } else {
            RectF(player.x - 8f, yPos - 12f, player.x + 12f, yPos + 18f)
        }
        canvas.drawRect(bodyRect, bodyPaint)

        // Cabeza / Casco
        val helmetColor = COLORS.primary.copy(alpha = alpha).toArgb()
        val helmetPaint = Paint().apply { color = helmetColor }
        val headY = if (player.isSliding) yPos + 4f else yPos - 12f
        canvas.drawRect(RectF(player.x - 6f, headY - 14f, player.x + 10f, headY), helmetPaint)
        
        // Piernas (Animación simple)
        if (!player.isJumping && !player.isSliding) {
            val legSwing = sin(frame * 0.4f) * 6f
            val legColor = Color.Black.copy(alpha = alpha).toArgb()
            val legPaint = Paint().apply { color = legColor }
            canvas.drawRect(player.x - 4f, yPos + 18f, player.x, yPos + 24f + legSwing, legPaint)
            canvas.drawRect(player.x + 4f, yPos + 18f, player.x + 8f, yPos + 24f - legSwing, legPaint)
        }
    }

    private fun drawObstacles(canvas: Canvas, obstacles: List<Obstacle>) {
        val paint = Paint().apply { color = COLORS.obstacle.toArgb() }
        val groundY = Player.FLOOR_Y + 20f
        obstacles.forEach { obs ->
            canvas.drawRect(
                obs.x, groundY - obs.height, obs.x + obs.width, groundY,
                paint
            )
        }
    }

    private fun drawCoins(canvas: Canvas, coins: List<Coin>, frame: Long) {
        val paint = Paint().apply { color = COLORS.coin.toArgb() }
        coins.forEach { coin ->
            if (!coin.collected) {
                val bounce = sin(frame * 0.1f + coin.phase) * 3f
                canvas.drawCircle(coin.x, coin.y + bounce, 8f, paint)
            }
        }
    }

    private fun drawHUD(canvas: Canvas, size: Size, state: GameState) {
        val cx = size.width / 2f
        drawCenteredText(canvas, getSystemTime(), cx, 30f, 14.sp, Color.White)
        drawCenteredText(canvas, "${state.score} pts", cx, size.height - 20f, 12.sp, Color.Yellow)
        
        // Ritmo cardíaco
        val hrColor = if (state.heartRate > 100) Color.Red else Color.Cyan
        drawCenteredText(canvas, "💓 ${state.heartRate} BPM", 60f, size.height - 20f, 10.sp, hrColor)

        // Vidas
        repeat(state.lives) { i ->
            drawHeart(canvas, 20f + i * 25f, 30f)
        }
    }

    private fun drawHeart(canvas: Canvas, x: Float, y: Float) {
        val paint = Paint().apply { 
            color = AndroidColor.RED
            isAntiAlias = true
        }
        canvas.drawCircle(x - 4f, y, 5f, paint)
        canvas.drawCircle(x + 4f, y, 5f, paint)
        
        val path = Path().apply {
            moveTo(x - 9f, y)
            lineTo(x + 9f, y)
            lineTo(x, y + 10f)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawCenteredText(canvas: Canvas, text: String, x: Float, y: Float, size: TextUnit, color: Color) {
        val paint = Paint().apply {
            this.color = color.toArgb()
            textSize = size.value * 2f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText(text, x, y, paint)
    }

    private fun getSystemTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}

class GameColors {
    val primary = Color(0xFF1A237E)
    val skyTop = Color(0xFF0D1B4A)
    val skyBottom = Color(0xFF1A237E)
    val ground = Color(0xFF2E7D32)
    val obstacle = Color(0xFFD32F2F)
    val coin = Color(0xFFFFD600)
}
