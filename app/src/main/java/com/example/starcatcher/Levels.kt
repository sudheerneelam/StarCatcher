package com.example.starcatcher

/**
 * One level's difficulty settings.
 * fallSpeed is in screen-heights per second, so it feels the same on every device.
 */
data class Level(
    val number: Int,
    val targetScore: Int,
    val fallSpeed: Float,
    val spawnIntervalMs: Long,
    val bombChance: Float
)

object Levels {
    const val count = 10

    private val all: List<Level> = (1..count).map { n ->
        Level(
            number = n,
            targetScore = 8 + n * 3,
            fallSpeed = 0.26f + 0.04f * n,
            spawnIntervalMs = (950 - n * 55).toLong(),
            bombChance = 0.12f + 0.03f * n
        )
    }

    fun get(number: Int): Level = all[(number - 1).coerceIn(0, count - 1)]
}
