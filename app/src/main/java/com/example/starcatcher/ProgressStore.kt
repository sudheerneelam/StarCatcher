package com.example.starcatcher

import android.content.Context

/** Remembers the highest unlocked level. */
object ProgressStore {
    private const val PREFS = "progress"
    private const val KEY_UNLOCKED = "unlocked"

    fun unlockedLevel(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_UNLOCKED, 1)

    fun unlock(context: Context, level: Int) {
        val target = level.coerceAtMost(Levels.count)
        if (target > unlockedLevel(context)) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_UNLOCKED, target).apply()
        }
    }
}
