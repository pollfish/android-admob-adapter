package com.prodege.example;

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
import com.prodege.mediation.ProdegeAdMobAdapter;
import com.prodege.mediation.ProdegeNetworkExtras;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RewardedAd mRewardedAd;
    private Button mRewardedAdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRewardedAdButton = findViewById(R.id.rewardedAdBtn);

        // This step is optional. By setting any of the following properties,
        // the corresponding property, if set on the AdMob dashboard, will be overwritten.
        ProdegeAdMobAdapter.setTestMode(true);
        ProdegeAdMobAdapter.setApiKey("API_KEY");
        ProdegeAdMobAdapter.setUserId("USER_ID");

        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "onInitializationComplete");
            createAndLoadRewardedAd();
        });
    }

    public void onRewardedAdButtonClick(View view) {
        if (mRewardedAd != null) {
            mRewardedAd.show(MainActivity.this, reward -> {
                String message =
                        String.format(Locale.getDefault(), "onUserEarnedReward: %d %s", reward.getAmount(), reward.getType());
                Log.d(TAG, message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't loaded yet.");
        }
    }

    public void createAndLoadRewardedAd() {
        mRewardedAdButton.setEnabled(false);

        // This step is also optional. By setting any of the following properties,
        // the corresponding property, if set on the AdMob dashboard, will be overwritten.
        Bundle extras = new ProdegeNetworkExtras.Builder()
                .placementId("PLACEMENT_ID")
                .muted(true)
                .requestUuid("REQUEST_UUID")
                .build();

        AdRequest request = new AdRequest.Builder()
                .addNetworkExtrasBundle(ProdegeAdMobAdapter.class, extras) // Optional, this should be used only if you've defined a ProdegeNetworkExtras object.
                .build();

        RewardedAd.load(this, getString(R.string.ad_mob_ad_unit_key), request, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
                mRewardedAdButton.setEnabled(true);

                Toast.makeText(MainActivity.this, "onRewardedAdLoaded", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onRewardedAdLoaded");
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        String message = String.format("onRewardedAdFailedToShow: %s", adError.getMessage());
                        Log.d(TAG, message);
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
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
                String message = String.format("onRewardedAdFailedToLoad: %s", loadAdError.getMessage());
                Log.d(TAG, message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                mRewardedAdButton.setEnabled(false);
                mRewardedAd = null;
            }

        });
    }

}