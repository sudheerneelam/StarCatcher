package com.example.starcatcher

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Handles the full-screen (interstitial) ads.
 * Banner ads live directly in the layouts (see activity_main.xml / activity_game.xml).
 */
object AdManager {
    // Show an interstitial after every 2nd finished level / game over.
    private const val SHOW_EVERY = 2

    private var interstitial: InterstitialAd? = null
    private var loading = false
    private var resultCount = 0

    fun init(context: Context) {
        MobileAds.initialize(context) {}
        loadInterstitial(context)
    }

    private fun loadInterstitial(context: Context) {
        if (loading || interstitial != null) return
        loading = true
        val appContext = context.applicationContext
        InterstitialAd.load(
            appContext,
            appContext.getString(R.string.admob_interstitial_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    loading = false
                }
            }
        )
    }

    /** Shows an interstitial if one is due and ready, then always calls [onFinished]. */
    fun showIfDue(activity: Activity, onFinished: () -> Unit) {
        resultCount++
        val ad = interstitial
        if (ad == null || resultCount % SHOW_EVERY != 0) {
            loadInterstitial(activity)
            onFinished()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial(activity)
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                loadInterstitial(activity)
                onFinished()
            }
        }
        ad.show(activity)
    }
}
