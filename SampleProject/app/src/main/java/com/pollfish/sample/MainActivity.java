package com.pollfish.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.ads.mediationtestsuite.MediationTestSuite;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.pollfish.mediation.PollfishAdMobAdapter;
import com.pollfish.mediation.PollfishExtrasBundleBuilder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RewardedAd rewardedAd;
    private Button rewardedAdBtn;

    //TODO: Replace with your own keys
    private String adMobKey="ADMOB_AD_UNIT_KEY";
    private String pollfishAPIKey="POLLFISH_API_KEY";
    private String adMobTestDevice="ADMOB_TEST_DEVICE_ID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //MediationTestSuite.launch(MainActivity.this);

        rewardedAdBtn = findViewById(R.id.rewardedAdBtn);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "onInitializationComplete()");
                createAndLoadRewardedAd();
            }
        });

        rewardedAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "rewardedAd.onClick()");

                if (rewardedAd.isLoaded()) {

                    Log.d(TAG, "rewardedAd.isLoaded()");

                    RewardedAdCallback adCallback = new RewardedAdCallback() {
                        @Override
                        public void onRewardedAdOpened() {
                            // Ad opened.

                            Log.d(TAG, "onRewardedAdOpened");

                            Toast.makeText(getApplicationContext(), "onRewardedAdOpened", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            // Ad closed.

                            Log.d(TAG, "onRewardedAdClosed");

                            Toast.makeText(getApplicationContext(), "onRewardedAdClosed", Toast.LENGTH_SHORT).show();

                            createAndLoadRewardedAd();
                        }

                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem reward) {
                            // User earned reward.

                            Log.d(TAG, "onUserEarnedReward of Type: " + reward.getType() + " and amount:"+  reward.getAmount());

                            Toast.makeText(getApplicationContext(), "onUserEarnedReward of Type: " + reward.getType() +" and amount:"+  reward.getAmount(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(int errorCode) {
                            // Ad failed to display

                            Log.d(TAG, "onRewardedAdFailedToShow");

                            Toast.makeText(getApplicationContext(), "onRewardedAdFailedToShow: " + getRewardedAdCallbackErrorCode(errorCode), Toast.LENGTH_SHORT).show();
                        }


                    };
                    rewardedAd.show(MainActivity.this, adCallback);
                } else {
                    Log.d(TAG, "The rewarded ad wasn't loaded yet.");
                }
            }
        });
    }


    public RewardedAd createAndLoadRewardedAd() {

        Log.d(TAG, "createAndLoadRewardedAd()");

        rewardedAdBtn.setVisibility(View.INVISIBLE);

        rewardedAd = new RewardedAd(this, adMobKey);

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.

                Toast.makeText(getApplicationContext(), "onRewardedAdLoaded", Toast.LENGTH_SHORT).show();

                rewardedAdBtn.setVisibility(View.VISIBLE);

            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.

                Toast.makeText(getApplicationContext(), "onRewardedAdFailedToLoad errorCode:" + getRewardedAdLoadErrorCode(errorCode), Toast.LENGTH_SHORT).show();

                rewardedAdBtn.setVisibility(View.INVISIBLE);
            }
        };

        Bundle pollfishBundle = new PollfishExtrasBundleBuilder()
                .setAPIKey(pollfishAPIKey)
                .setReleaseMode(false)
                .setRequestUUID("MY_ID")
                .build();

        AdRequest request = new AdRequest.Builder()
                .addTestDevice(adMobTestDevice)
                .addNetworkExtrasBundle(PollfishAdMobAdapter.class,pollfishBundle)
                .build();

        rewardedAd.loadAd(request, adLoadCallback);

        return rewardedAd;
    }


    private String getRewardedAdLoadErrorCode(int errorCode) {
        switch (errorCode) {
            case 0:
                return "Internal Error";
            case 1:
                return "Invalid Request";
            case 2:
                return "Network Error";
            case 3:
                return "No Fill";
            default:
                return "Unknown";
        }
    }

    private String getRewardedAdCallbackErrorCode(int errorCode) {
        switch (errorCode) {
            case 0:
                return "Internal Error";
            case 1:
                return "Ad Reused";
            case 2:
                return "Not Ready";
            case 3:
                return "App not in foreground";
            default:
                return "Unknown";
        }
    }
}