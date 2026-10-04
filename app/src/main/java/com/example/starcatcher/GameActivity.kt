package com.example.starcatcher

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class GameActivity : AppCompatActivity(), GameView.Listener {

    private lateinit var gameView: GameView
    private var level = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        level = intent.getIntExtra(EXTRA_LEVEL, 1)
        gameView = findViewById(R.id.gameView)
        gameView.listener = this
        findViewById<AdView>(R.id.adView).loadAd(AdRequest.Builder().build())
        gameView.startLevel(level)
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onLevelComplete(level: Int, score: Int) {
        ProgressStore.unlock(this, level + 1)
        AdManager.showIfDue(this) { showResultDialog(won = true, score = score) }
    }

    override fun onGameOver(level: Int, score: Int) {
        AdManager.showIfDue(this) { showResultDialog(won = false, score = score) }
    }

    private fun showResultDialog(won: Boolean, score: Int) {
        if (isFinishing || isDestroyed) return

        val builder = AlertDialog.Builder(this).setCancelable(false)
        if (won) {
            if (level < Levels.count) {
                builder.setTitle(R.string.level_complete)
                    .setMessage(getString(R.string.score_fmt, score))
                    .setPositiveButton(R.string.next_level) { _, _ ->
                        level++
                        gameView.startLevel(level)
                    }
            } else {
                builder.setTitle(R.string.all_done).setMessage(R.string.all_done_msg)
            }
            builder.setNeutralButton(R.string.replay) { _, _ -> gameView.startLevel(level) }
        } else {
            builder.setTitle(R.string.game_over)
                .setMessage(getString(R.string.game_over_fmt, score))
                .setPositiveButton(R.string.retry) { _, _ -> gameView.startLevel(level) }
        }
        builder.setNegativeButton(R.string.menu) { _, _ -> finish() }
        builder.show()
    }

    companion object {
        const val EXTRA_LEVEL = "level"
    }
}
