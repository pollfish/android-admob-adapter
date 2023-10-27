package com.prodege.mediation.admob.example

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.prodege.mediation.ProdegeAdMobAdapter
import com.prodege.mediation.ProdegeNetworkExtras
import com.prodege.mediation.admob.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var mRewardedAd: RewardedAd? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // This step is optional. By setting any of the following properties,
        // the corresponding property, if set on the AdMob dashboard, will be overwritten.
        ProdegeAdMobAdapter.setTestMode(true)
        ProdegeAdMobAdapter.setApiKey("API_KEY")
        ProdegeAdMobAdapter.setUserId("USER_ID")

        MobileAds.initialize(this) {
            Log.d(TAG, "onInitializationComplete()")
            createAndLoadRewardedAd()
        }

        binding.rewardedAdBtn.setOnClickListener {
            if (mRewardedAd != null) {
                mRewardedAd!!.show(this@MainActivity) { reward: RewardItem ->
                    val message = "onUserEarnedReward: ${reward.amount} ${reward.type}"
                    Log.d(TAG, message)
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d(TAG, "The rewarded ad wasn't loaded yet.")
            }
        }
    }

    private fun createAndLoadRewardedAd() {
        binding.rewardedAdBtn.isEnabled = false

        // This step is also optional. By setting any of the following properties,
        // the corresponding property, if set on the AdMob dashboard, will be overwritten.
        val extras = ProdegeNetworkExtras.Builder()
            .placementId("PLACEMENT_ID")
            .muted(true)
            .requestUuid("REQUEST_UUID")
            .build()

        val request = AdRequest.Builder()
            .addNetworkExtrasBundle(
                ProdegeAdMobAdapter::class.java,
                extras
            ) // Optional, this should be used only if you've defined a ProdegeNetworkExtras object.
            .build()

        RewardedAd.load(
            this,
            getString(R.string.ad_mob_ad_unit_key),
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    binding.rewardedAdBtn.isEnabled = true
                    Toast.makeText(this@MainActivity, "onRewardedAdLoaded", Toast.LENGTH_SHORT)
                        .show()
                    Log.d(TAG, "onRewardedAdLoaded")
                    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            val message = "onRewardedAdFailedToShow: ${adError.message}"
                            Log.d(TAG, message)
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                        }

                        override fun onAdShowedFullScreenContent() {

                            Log.d(TAG, "onRewardedAdOpened")
                            Toast.makeText(
                                this@MainActivity,
                                "onRewardedAdOpened",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "onRewardedAdClosed")
                            Toast.makeText(
                                this@MainActivity,
                                "onRewardedAdClosed",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            createAndLoadRewardedAd()
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    val message = "onRewardedAdFailedToLoad: ${loadAdError.message}"
                    Log.d(TAG, message)
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    binding.rewardedAdBtn.isEnabled = false
                    mRewardedAd = null
                }
            })
    }

    companion object {
        const val TAG = "MainActivity"
    }

}