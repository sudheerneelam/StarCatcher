package com.example.starcatcher

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class MainActivity : AppCompatActivity() {

    private lateinit var grid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        grid = findViewById(R.id.levelGrid)
        findViewById<AdView>(R.id.adView).loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        buildLevelButtons()
    }

    private fun buildLevelButtons() {
        grid.removeAllViews()
        val unlocked = ProgressStore.unlockedLevel(this)
        val margin = (8 * resources.displayMetrics.density).toInt()
        val height = (72 * resources.displayMetrics.density).toInt()

        for (n in 1..Levels.count) {
            val isUnlocked = n <= unlocked
            val button = Button(this).apply {
                text = if (isUnlocked) n.toString() else "🔒"
                textSize = 22f
                gravity = Gravity.CENTER
                isEnabled = isUnlocked
                setOnClickListener {
                    startActivity(
                        Intent(this@MainActivity, GameActivity::class.java)
                            .putExtra(GameActivity.EXTRA_LEVEL, n)
                    )
                }
            }
            val params = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                this.height = height
                setMargins(margin, margin, margin, margin)
            }
            grid.addView(button, params)
        }
    }
}
