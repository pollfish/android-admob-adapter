package com.pollfish.mediation;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.pollfish.Pollfish;
import com.pollfish.builder.Params;
import com.pollfish.builder.Platform;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

public class PollfishAdMobAdapter extends Adapter implements
        MediationRewardedAd {

    static final String TAG = "PollfishAdMobMediation";

    /**
     * Mediation rewarded video ad listener used to forward rewarded ad events
     * from {@link Pollfish} to the Google Mobile Ads SDK.
     */
    private MediationRewardedAdCallback mMediationRewardedAdCallback;

    /**
     * Pollfish API SDK API key
     */
    private String pollfishAPIKey;

    /**
     * Pollfish release mode
     */
    private boolean releaseMode = true;

    /**
     * Pollfish Request UUID
     */
    private String requestUUID;

    /**
     * Pollfish Offerwall mode
     */
    private boolean offerwallMode = false;

    /**
     * WeakReference of context to avoid memory leaks
     */
    private WeakReference<Context> contextWeakRef;

    @Override
    public void initialize(@NotNull Context context,
                           @NotNull InitializationCompleteCallback initializationCompleteCallback,
                           @NotNull List<MediationConfiguration> mediationConfigurations) {

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "initialize()");

        if (!(context instanceof Activity)) {
            initializationCompleteCallback.onInitializationFailed(
                    "Pollfish SDK requires an Activity context to initialize");
            return;
        }

        // Pollfish SDK does not have any API for initialization.
        initializationCompleteCallback.onInitializationSucceeded();
    }

    @Override
    public void loadRewardedAd(@NotNull MediationRewardedAdConfiguration mediationRewardedAdConfiguration,
                               @NotNull MediationAdLoadCallback<MediationRewardedAd,
                                       MediationRewardedAdCallback> mediationAdLoadCallback) {
        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd()");

        if (Build.VERSION.SDK_INT < 21) {
            Log.d(TAG, "Pollfish surveys will not run on targets lower than 21");
            mediationAdLoadCallback.onFailure("Pollfish surveys will not run on targets lower than 21");
            return;
        }

        setContext(mediationRewardedAdConfiguration.getContext());

        if (Pollfish.isPollfishPanelOpen()) {

            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                Log.d(TAG, "Pollfish Survey Panel already visible");

            mediationAdLoadCallback.onFailure("Pollfish Survey Panel already visible");
            return;
        }

        if (!(getContext() instanceof Activity)) {
            mediationAdLoadCallback.onFailure("Context is not an Activity. Pollfish requires an Activity context to load surveys.");
            return;
        }

        if (PollfishAdMobAdapterConstants.DEBUGMODE)
            Log.d(TAG, "MediationRewardedAdConfiguration:" + mediationRewardedAdConfiguration.getServerParameters().toString());

        Bundle serverParameters = mediationRewardedAdConfiguration.getServerParameters();
        Bundle networkExtras = mediationRewardedAdConfiguration.getMediationExtras();

        if (networkExtras != null) {

            pollfishAPIKey = networkExtras.getString(PollfishExtrasBundleBuilder.POLLFISH_API_KEY);
            releaseMode = networkExtras.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_MODE, true);
            requestUUID = networkExtras.getString(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID);
            offerwallMode = networkExtras.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_INTEGRATION_TYPE, false);

            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                Log.d(TAG, "loadRewardedAd() networkExtras key: " + pollfishAPIKey);
            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                Log.d(TAG, "loadRewardedAd() networkExtras mode: " + releaseMode);
            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                Log.d(TAG, "loadRewardedAd() networkExtras requestUUID: " + requestUUID);
            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                Log.d(TAG, "loadRewardedAd() networkExtras offerwallMode: " + offerwallMode);
        }

        if (serverParameters != null) {

            try {

                String jsonParams = serverParameters.getString("parameter");

                try {

                    JSONObject jsonObject = new JSONObject(jsonParams);

                    if (jsonObject != null) {

                        Log.d(TAG, "Pollfish jsonParams: " + jsonObject.toString());

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_API_KEY)) {
                            //Checking address Key Present or not
                            pollfishAPIKey = jsonObject.getString(PollfishExtrasBundleBuilder.POLLFISH_API_KEY); // Present Key

                            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                                Log.d(TAG, "Pollfish API Key from AdMob UI: " + pollfishAPIKey);
                        }

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID)) {

                            requestUUID = jsonObject.getString(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID); // Present Key

                            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                                Log.d(TAG, "Pollfish requestUUID from AdMob UI: " + requestUUID);
                        }

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_INTEGRATION_TYPE)) {

                            offerwallMode = jsonObject.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_INTEGRATION_TYPE); // Present Key

                            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                                Log.d(TAG, "Pollfish offerwallMode from AdMob UI: " + offerwallMode);
                        }

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_MODE)) {

                            releaseMode = jsonObject.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_MODE); // Present Key

                            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                                Log.d(TAG, "Pollfish releaseMode from AdMob UI: " + releaseMode);
                        }

                    }

                } catch (Throwable t) {
                    Log.e(TAG, "Could not parse malformed JSON: " + jsonParams);
                }

            } catch (Exception e) {
                if (PollfishAdMobAdapterConstants.DEBUGMODE)
                    Log.e(TAG, "loadRewardedAd() exception: " + e);
            }
        }

        if (TextUtils.isEmpty(pollfishAPIKey)) {
            Log.d(TAG, "Pollfish SDK Failed: Missing Pollfish API Key");
            return;
        }

        if (PollfishAdMobAdapterConstants.DEBUGMODE)
            Log.d(TAG, "pollfishAPIKey(): " + pollfishAPIKey);

        Pollfish.initWith((Activity) getContext(), new Params.Builder(pollfishAPIKey)
                .releaseMode(releaseMode)
                .platform(Platform.ADMOB)
                .requestUUID(requestUUID)
                .pollfishSurveyReceivedListener(surveyInfo -> {
                    if (PollfishAdMobAdapterConstants.DEBUGMODE)
                        Log.d(TAG, "onPollfishSurveyReceived");

                    mMediationRewardedAdCallback = mediationAdLoadCallback.onSuccess(PollfishAdMobAdapter.this);
                })
                .pollfishSurveyNotAvailableListener(() -> {
                    if (PollfishAdMobAdapterConstants.DEBUGMODE)
                        Log.d(TAG, "onPollfishSurveyNotAvailable");

                    mediationAdLoadCallback.onFailure("Pollfish Surveys Not Available");
                })
                .pollfishOpenedListener(() -> {
                    if (PollfishAdMobAdapterConstants.DEBUGMODE)
                        Log.d(TAG, "onPollfishOpened");

                    if (mMediationRewardedAdCallback != null) {
                        mMediationRewardedAdCallback.onAdOpened();
                        mMediationRewardedAdCallback.onVideoStart();
                        mMediationRewardedAdCallback.reportAdImpression();
                    }
                })
                .pollfishClosedListener(() -> {
                    if (PollfishAdMobAdapterConstants.DEBUGMODE)
                        Log.d(TAG, "onPollfishClosed");

                    if (mMediationRewardedAdCallback != null) {
                        mMediationRewardedAdCallback.onAdClosed();
                    }
                })
                .pollfishSurveyCompletedListener(surveyInfo -> {

                    final RewardItem reward;

                    if (surveyInfo.getRewardValue() == null) {
                        reward = RewardItem.DEFAULT_REWARD;
                        Log.d(TAG, "You are about to reward the user with AdMob's default reward. In order to define a custom reward name and exchange rate, please enable Reward settings on the Pollfish Publisher's Dashboard - App Settings area");
                    } else {
                        reward = new PollfishReward(surveyInfo.getRewardName(), surveyInfo.getRewardValue());
                    }

                    if (PollfishAdMobAdapterConstants.DEBUGMODE)
                        Log.d(TAG, "onPollfishSurveyCompleted");

                    if (mMediationRewardedAdCallback != null) {
                        mMediationRewardedAdCallback.onUserEarnedReward(reward);
                        mMediationRewardedAdCallback.onVideoComplete();
                    }
                })
                .rewardMode(true)
                .offerwallMode(offerwallMode)
                .build());
    }

    @Override
    public void showAd(@NotNull Context context) {
        if (Build.VERSION.SDK_INT < 21) {
            mMediationRewardedAdCallback.onAdFailedToShow("Pollfish can't run on Android SDK 20 or lower");
            return;
        }

        if (PollfishAdMobAdapterConstants.DEBUGMODE)
            Log.d(TAG, "showAd()");

        if (mMediationRewardedAdCallback != null) {
            mMediationRewardedAdCallback.reportAdClicked();
        }

        Pollfish.show();
    }

    @NotNull
    @Override
    public VersionInfo getVersionInfo() {

        String versionString = PollfishAdMobAdapterConstants.POLLFISH_ADAPTER_VERSION;

        String[] splits = versionString.split("\\.");

        if (splits.length >= 4) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]) * 100 + Integer.parseInt(splits[3]);

            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                Log.d(TAG, "getVersionInfo():" + major + " " + minor + " " + micro);

            return new VersionInfo(major, minor, micro);
        }

        Log.d(TAG, String.format("Unexpected adapter version format: %s." +
                "Returning 0.0.0 for adapter version.", versionString));

        return new VersionInfo(0, 0, 0);
    }

    @NotNull
    @Override
    public VersionInfo getSDKVersionInfo() {

        String versionString = PollfishAdMobAdapterConstants.POLLFISH_ADAPTER_VERSION;

        String[] splits = versionString.split("\\.");

        if (splits.length >= 3) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]);

            Log.d(TAG, "getSDKVersionInfo():" + major + " " + minor + " " + micro);

            return new VersionInfo(major, minor, micro);
        }

        Log.d(TAG, String.format("Unexpected adapter version format: %s." +
                "Returning 0.0.0 for adapter version.", versionString));

        return new VersionInfo(0, 0, 0);
    }

    private void setContext(final Context context) {
        contextWeakRef = new WeakReference<>(context);
    }

    private Context getContext() {
        if (contextWeakRef == null) {
            return null;
        }

        return contextWeakRef.get();
    }

}
