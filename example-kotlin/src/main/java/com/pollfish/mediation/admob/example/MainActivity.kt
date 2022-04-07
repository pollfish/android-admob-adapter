package com.pollfish.mediation.admob.example

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.pollfish.mediation.PollfishAdMobAdapter
import com.pollfish.mediation.PollfishExtrasBundleBuilder
import com.pollfish.mediation.admob.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val AD_MOB_AD_UNIT_KEY = "ADMOB_AD_UNIT_KEY"
        const val POLLFISH_API_KEY = "POLLFISH_API_KEY"
    }

    private var mRewardedAd: RewardedAd? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        MobileAds.initialize(this) {
            Log.d(TAG, "onInitializationComplete()")
            createAndLoadRewardedAd()
        }

        binding.rewardedAdBtn.setOnClickListener {
            Log.d(TAG, "rewardedAd.onClick()")

            if (mRewardedAd != null) {
                mRewardedAd!!.show(this@MainActivity) { reward: RewardItem ->
                    Log.d(
                        TAG,
                        "onUserEarnedReward of Type: ${reward.type} and amount: ${reward.amount}"
                    )
                    Toast.makeText(
                        applicationContext,
                        "onUserEarnedReward of Type: ${reward.type} and amount: ${reward.amount}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.d(TAG, "The rewarded ad wasn't loaded yet.")
            }
        }
    }

    private fun createAndLoadRewardedAd() {
        binding.rewardedAdBtn.isEnabled = false

        val bundle = PollfishExtrasBundleBuilder()
            .setAPIKey(POLLFISH_API_KEY)
            .setReleaseMode(false)
            .setRequestUUID("MY_UUID")
            .build()

        val request = AdRequest.Builder()
            .addNetworkExtrasBundle(PollfishAdMobAdapter::class.java, bundle)
            .build()

        RewardedAd.load(this, AD_MOB_AD_UNIT_KEY, request, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                mRewardedAd = rewardedAd
                binding.rewardedAdBtn.isEnabled = true
                Toast.makeText(this@MainActivity, "onRewardedAdLoaded", Toast.LENGTH_SHORT).show()
                rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d(TAG, "onRewardedAdFailedToShow")
                        Toast.makeText(
                            applicationContext,
                            "onRewardedAdFailedToShow: ${adError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "onRewardedAdOpened")
                        Toast.makeText(applicationContext, "onRewardedAdOpened", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "onRewardedAdClosed")
                        Toast.makeText(applicationContext, "onRewardedAdClosed", Toast.LENGTH_SHORT)
                            .show()
                        createAndLoadRewardedAd()
                    }
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Toast.makeText(
                    applicationContext,
                    "onRewardedAdFailedToLoad errorCode: ${loadAdError.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.rewardedAdBtn.isEnabled = false
                mRewardedAd = null
            }
        })
    }
}