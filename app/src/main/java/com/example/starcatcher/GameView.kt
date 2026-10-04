package com.example.starcatcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The whole game. Drag your finger to move the basket.
 * Catch stars to reach the level's target score; catching a bomb costs a life.
 * All positions are stored as fractions of the screen (0..1) so it works on any device.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface Listener {
        fun onLevelComplete(level: Int, score: Int)
        fun onGameOver(level: Int, score: Int)
    }

    private class Item(var x: Float, var y: Float, val bomb: Boolean)

    var listener: Listener? = null

    private var level: Level = Levels.get(1)
    private val items = ArrayList<Item>()
    private var score = 0
    private var lives = START_LIVES
    private var basketX = 0.5f
    private var spawnTimerMs = 0f
    private var running = false
    private var finished = false
    private var lastNanos = 0L

    private val starPath = Path()
    private val basketRect = RectF()

    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFD54F") }
    private val bombPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#37474F") }
    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#78909C") }
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF5252") }
    private val fusePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BCAAA4")
        style = Paint.Style.STROKE
    }
    private val basketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#26C6DA") }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

    fun startLevel(number: Int) {
        level = Levels.get(number)
        items.clear()
        score = 0
        lives = START_LIVES
        spawnTimerMs = 0f
        finished = false
        running = true
        lastNanos = 0L
        invalidate()
    }

    fun pause() {
        running = false
    }

    fun resume() {
        if (!finished) {
            running = true
            lastNanos = 0L
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (width > 0) {
                    basketX = (event.x / width).coerceIn(BASKET_HALF_W, 1f - BASKET_HALF_W)
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        if (running && !finished) step()

        canvas.drawColor(Color.parseColor("#0B1026"))
        drawItems(canvas, w, h)
        drawBasket(canvas, w, h)
        drawHud(canvas, w)

        if (running && !finished) postInvalidateOnAnimation()
    }

    // ---------- game logic ----------

    private fun step() {
        val now = System.nanoTime()
        val dt = if (lastNanos == 0L) 0f else ((now - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
        lastNanos = now

        spawnTimerMs -= dt * 1000f
        if (spawnTimerMs <= 0f) {
            spawnTimerMs = level.spawnIntervalMs.toFloat()
            val x = Random.nextFloat() * (1f - 2 * ITEM_R) + ITEM_R
            items.add(Item(x, -0.05f, Random.nextFloat() < level.bombChance))
        }

        val iterator = items.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            item.y += level.fallSpeed * dt

            val inCatchZone = item.y >= BASKET_Y - 0.03f && item.y <= BASKET_Y + 0.04f
            val caught = inCatchZone && abs(item.x - basketX) <= BASKET_HALF_W + ITEM_R * 0.6f

            if (caught) {
                iterator.remove()
                if (item.bomb) lives-- else score++
            } else if (item.y > 1.1f) {
                iterator.remove()
            }
        }

        if (lives <= 0) {
            finish(won = false)
        } else if (score >= level.targetScore) {
            finish(won = true)
        }
    }

    private fun finish(won: Boolean) {
        finished = true
        running = false
        val finalScore = score
        val levelNumber = level.number
        post {
            if (won) listener?.onLevelComplete(levelNumber, finalScore)
            else listener?.onGameOver(levelNumber, finalScore)
        }
    }

    // ---------- drawing ----------

    private fun drawItems(canvas: Canvas, w: Float, h: Float) {
        val radius = ITEM_R * w
        for (item in items) {
            val cx = item.x * w
            val cy = item.y * h
            if (item.bomb) drawBomb(canvas, cx, cy, radius) else drawStar(canvas, cx, cy, radius)
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        starPath.reset()
        for (i in 0 until 10) {
            val radius = if (i % 2 == 0) r else r * 0.45f
            val angle = -Math.PI / 2 + i * Math.PI / 5
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()
        canvas.drawPath(starPath, starPaint)
    }

    private fun drawBomb(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        canvas.drawCircle(cx, cy, r * 0.9f, bombPaint)
        canvas.drawCircle(cx - r * 0.3f, cy - r * 0.3f, r * 0.2f, shinePaint)
        fusePaint.strokeWidth = r * 0.12f
        canvas.drawLine(cx, cy - r * 0.85f, cx + r * 0.3f, cy - r * 1.25f, fusePaint)
        canvas.drawCircle(cx + r * 0.3f, cy - r * 1.25f, r * 0.17f, sparkPaint)
    }

    private fun drawBasket(canvas: Canvas, w: Float, h: Float) {
        val top = BASKET_Y * h
        basketRect.set(
            (basketX - BASKET_HALF_W) * w,
            top,
            (basketX + BASKET_HALF_W) * w,
            top + h * 0.035f
        )
        val corner = h * 0.017f
        canvas.drawRoundRect(basketRect, corner, corner, basketPaint)
    }

    private fun drawHud(canvas: Canvas, w: Float) {
        textPaint.textSize = w * 0.05f
        val pad = w * 0.04f
        val line1 = textPaint.textSize + pad

        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Level ${level.number}", pad, line1, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("♥".repeat(lives.coerceAtLeast(0)), w - pad, line1, textPaint)

        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Stars $score / ${level.targetScore}", pad, line1 + textPaint.textSize * 1.4f, textPaint)
    }

    companion object {
        private const val START_LIVES = 3
        private const val BASKET_Y = 0.9f
        private const val BASKET_HALF_W = 0.12f
        private const val ITEM_R = 0.045f
    }
}
