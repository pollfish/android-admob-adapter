package com.pollfish.mediation;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.AdError;
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

    private static final String DOMAIN = "com.pollfish.mediation.PollfishAdMobAdapter";

    private static final int ERROR_CODE_NOT_AVAILABLE = 0;
    private static final int ERROR_CODE_LOW_TARGET = 1;
    private static final int ERROR_CODE_PANEL_ALREADY_VISIBLE = 2;
    private static final int ERROR_CODE_WRONG_CONTEXT = 3;
    private static final int ERROR_CODE_EMPTY_API_KEY = 4;

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

        initializationCompleteCallback.onInitializationSucceeded();
    }

    @Override
    public void loadRewardedAd(@NotNull MediationRewardedAdConfiguration mediationRewardedAdConfiguration,
                               @NotNull MediationAdLoadCallback<MediationRewardedAd,
                                       MediationRewardedAdCallback> mediationAdLoadCallback) {
        if (Build.VERSION.SDK_INT < 21) {
            Log.e(TAG, "Pollfish surveys will not run on targets lower than 21");
            mediationAdLoadCallback.onFailure(new AdError(ERROR_CODE_LOW_TARGET, "Pollfish surveys will not run on targets lower than 21", DOMAIN));
            return;
        }

        setContext(mediationRewardedAdConfiguration.getContext());

        if (Pollfish.isPollfishPanelOpen()) {
            mediationAdLoadCallback.onFailure(new AdError(ERROR_CODE_PANEL_ALREADY_VISIBLE, "Pollfish Survey Panel already visible", DOMAIN));
            return;
        }

        if (!(getContext() instanceof Activity)) {
            mediationAdLoadCallback.onFailure(new AdError(ERROR_CODE_WRONG_CONTEXT, "Context is not an Activity. Pollfish requires an Activity context to load surveys.", DOMAIN));
            return;
        }

        Bundle serverParameters = mediationRewardedAdConfiguration.getServerParameters();
        Bundle networkExtras = mediationRewardedAdConfiguration.getMediationExtras();

        if (serverParameters != null) {
            try {
                String jsonParams = serverParameters.getString("parameter");

                try {
                    JSONObject jsonObject = new JSONObject(jsonParams);

                    if (jsonObject != null) {
                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_API_KEY)) {
                            pollfishAPIKey = jsonObject.getString(PollfishExtrasBundleBuilder.POLLFISH_API_KEY);
                        }

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID)) {
                            requestUUID = jsonObject.getString(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID);
                        }

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_INTEGRATION_TYPE)) {
                            offerwallMode = jsonObject.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_INTEGRATION_TYPE);
                        }

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_MODE)) {
                            releaseMode = jsonObject.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_MODE);
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

        if (networkExtras != null) {
            if (networkExtras.containsKey(PollfishExtrasBundleBuilder.POLLFISH_API_KEY)) {
                pollfishAPIKey = networkExtras.getString(PollfishExtrasBundleBuilder.POLLFISH_API_KEY);
            }

            if (networkExtras.containsKey(PollfishExtrasBundleBuilder.POLLFISH_MODE)) {
                releaseMode = networkExtras.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_MODE, true);
            }

            if (networkExtras.containsKey(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID)) {
                requestUUID = networkExtras.getString(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID);
            }

            if (networkExtras.containsKey(PollfishExtrasBundleBuilder.POLLFISH_INTEGRATION_TYPE)) {
                offerwallMode = networkExtras.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_INTEGRATION_TYPE, false);
            }
        }

        if (TextUtils.isEmpty(pollfishAPIKey)) {
            Log.e(TAG, "Pollfish SDK Failed: Missing Pollfish API Key");
            mediationAdLoadCallback.onFailure(new AdError(ERROR_CODE_EMPTY_API_KEY, "Missing Pollfish API Key.", DOMAIN));
            return;
        }

        Pollfish.initWith((Activity) getContext(), new Params.Builder(pollfishAPIKey)
                .releaseMode(releaseMode)
                .platform(Platform.ADMOB)
                .requestUUID(requestUUID)
                .pollfishSurveyReceivedListener(surveyInfo -> mMediationRewardedAdCallback = mediationAdLoadCallback.onSuccess(PollfishAdMobAdapter.this))
                .pollfishSurveyNotAvailableListener(() -> mediationAdLoadCallback.onFailure(new AdError(ERROR_CODE_NOT_AVAILABLE, "Pollfish Surveys Not Available", DOMAIN)))
                .pollfishOpenedListener(() -> {
                    if (mMediationRewardedAdCallback != null) {
                        mMediationRewardedAdCallback.onAdOpened();
                        mMediationRewardedAdCallback.onVideoStart();
                        mMediationRewardedAdCallback.reportAdImpression();
                    }
                })
                .pollfishClosedListener(() -> {
                    if (mMediationRewardedAdCallback != null) {
                        mMediationRewardedAdCallback.onAdClosed();
                    }
                })
                .pollfishSurveyCompletedListener(surveyInfo -> {
                    final RewardItem reward;

                    if (surveyInfo.getRewardValue() == null) {
                        reward = RewardItem.DEFAULT_REWARD;
                        Log.i(TAG, "You are about to reward the user with AdMob's default reward. In order to define a custom reward name and exchange rate, please enable Reward settings on the Pollfish Publisher's Dashboard - App Settings area");
                    } else {
                        reward = new PollfishReward(surveyInfo.getRewardName(), surveyInfo.getRewardValue());
                    }

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
            mMediationRewardedAdCallback.onAdFailedToShow(new AdError(ERROR_CODE_LOW_TARGET, "Pollfish surveys will not run on targets lower than 21", DOMAIN));
            return;
        }

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
