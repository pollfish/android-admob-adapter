package com.pollfish.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.pollfish.mediation.PollfishAdMobAdapter;
import com.pollfish.mediation.PollfishExtrasBundleBuilder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //TODO: Replace with your own keys
    private static final String AD_MOB_KEY = "ADMOB_AD_UNIT_KEY";
    private static final String POLLFISH_API_KEY = "POLLFISH_API_KEY";

    private RewardedAd mRewardedAd;
    private Button mRewardedAdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRewardedAdButton = findViewById(R.id.rewardedAdBtn);

        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "onInitializationComplete()");
            createAndLoadRewardedAd();
        });

        mRewardedAdButton.setOnClickListener(v -> {

            Log.d(TAG, "rewardedAd.onClick()");

            if (mRewardedAd != null) {

                mRewardedAd.show(MainActivity.this, reward -> {
                    Log.d(TAG, "onUserEarnedReward of Type: " + reward.getType() + " and amount:" + reward.getAmount());

                    Toast.makeText(getApplicationContext(), "onUserEarnedReward of Type: " + reward.getType() + " and amount:" + reward.getAmount(), Toast.LENGTH_SHORT).show();
                });

            } else {
                Log.d(TAG, "The rewarded ad wasn't loaded yet.");
            }
        });
    }

    public void createAndLoadRewardedAd() {
        Log.d(TAG, "createAndLoadRewardedAd()");

        mRewardedAdButton.setVisibility(View.GONE);

        Bundle pollfishBundle = new PollfishExtrasBundleBuilder()
                .setAPIKey(POLLFISH_API_KEY)
                .setReleaseMode(false)
                .setRequestUUID("MY_ID")
                .build();

        AdRequest request = new AdRequest.Builder()
                .addNetworkExtrasBundle(PollfishAdMobAdapter.class, pollfishBundle)
                .build();

        RewardedAd.load(this, AD_MOB_KEY, request, new RewardedAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;

                mRewardedAdButton.setVisibility(View.VISIBLE);

                Toast.makeText(MainActivity.this, "onRewardedAdLoaded", Toast.LENGTH_SHORT).show();

                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.d(TAG, "onRewardedAdFailedToShow");

                        Toast.makeText(getApplicationContext(), "onRewardedAdFailedToShow: " + adError.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "onRewardedAdOpened");

                        Toast.makeText(getApplicationContext(), "onRewardedAdOpened", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "onRewardedAdClosed");

                        Toast.makeText(getApplicationContext(), "onRewardedAdClosed", Toast.LENGTH_SHORT).show();

                        createAndLoadRewardedAd();
                    }

                });

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Toast.makeText(getApplicationContext(), "onRewardedAdFailedToLoad errorCode:" + loadAdError.getMessage(), Toast.LENGTH_SHORT).show();

                mRewardedAdButton.setVisibility(View.GONE);

                mRewardedAd = null;
            }

        });
    }

}