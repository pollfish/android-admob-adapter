package com.pollfish.mediation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;
import com.pollfish.classes.SurveyInfo;
import com.pollfish.interfaces.PollfishClosedListener;
import com.pollfish.interfaces.PollfishCompletedSurveyListener;
import com.pollfish.interfaces.PollfishOpenedListener;
import com.pollfish.interfaces.PollfishReceivedSurveyListener;
import com.pollfish.interfaces.PollfishSurveyNotAvailableListener;
import com.pollfish.interfaces.PollfishUserNotEligibleListener;
import com.pollfish.interfaces.PollfishUserRejectedSurveyListener;
import com.pollfish.main.PollFish;

import java.lang.ref.WeakReference;
import java.util.List;

public class PollfishAdMobAdapter extends Adapter implements
        MediationRewardedAd {

    static final String TAG = "PollfishAdMobMediation";

    /**
     * Mediation listener used to forward rewarded ad events from
     * {@link PollFish} to Google Mobile Ads SDK for loading phases of the ad
     */
    private MediationAdLoadCallback<MediationRewardedAd,
            MediationRewardedAdCallback> mMediationAdLoadCallback;

    /**
     * Mediation rewarded video ad listener used to forward rewarded ad events
     * from {@link PollFish} to the Google Mobile Ads SDK.
     */
    private MediationRewardedAdCallback mMediationRewardedAdCallback;


    /**
     * Pollfish API SDK API key
     */
    private String pollfishAPIKey;

    /**
     * Pollfish release mode
     */
    private boolean releaseMode;

    /**
     * Pollfish Request UUID
     */
    private String requestUUID;


    /**
     * WeakReference of context to avoid memory leaks
     */
    private WeakReference<Context> contextWeakRef;


    @Override
    public void initialize(Context context, InitializationCompleteCallback initializationCompleteCallback, List<MediationConfiguration> mediationConfigurations) {

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "initialize()");

        if (!(context instanceof Activity)) {
            initializationCompleteCallback.onInitializationFailed("Pollfish SDK requires an Activity context to initialize");
            return;
        }

        // Pollfish SDK does not have any API for initialization.

        initializationCompleteCallback.onInitializationSucceeded();

    }

    @Override
    public void loadRewardedAd(MediationRewardedAdConfiguration mediationRewardedAdConfiguration,
                               final MediationAdLoadCallback<MediationRewardedAd,
                                       MediationRewardedAdCallback> mediationAdLoadCallback) {
        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd()");

        setContext(mediationRewardedAdConfiguration.getContext());

        if (!(getContext() instanceof Activity)) {
            mediationAdLoadCallback.onFailure("Context is not an Activity. Pollfish requires an Activity context to load surveys.");
            return;
        }

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "MediationRewardedAdConfiguration:" + mediationRewardedAdConfiguration.getServerParameters().toString());


        Bundle serverParameters = mediationRewardedAdConfiguration.getServerParameters();
        Bundle networkExtras = mediationRewardedAdConfiguration.getMediationExtras();

        if (networkExtras != null) {

            pollfishAPIKey = networkExtras.getString(PollfishExtrasBundleBuilder.POLLFISH_API_KEY);
            releaseMode = networkExtras.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_MODE, false);
            requestUUID = networkExtras.getString(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID);

            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd() networkExtras key: " + pollfishAPIKey);
            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd() networkExtras mode: " + releaseMode);
            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd() networkExtras requestUUID: " + requestUUID);
        }

        if (serverParameters != null) {

            try {
                String pollfishAPIKeyTmp = serverParameters.getString("parameter");

                if(pollfishAPIKeyTmp!=null) {
                    pollfishAPIKey = pollfishAPIKeyTmp;
                }
            } catch (Exception e) {
                if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.e(TAG, "loadRewardedAd() exception: " + e);

            }
        }


        if (TextUtils.isEmpty(pollfishAPIKey)) {
            Log.d(TAG, "Pollfish SDK Failed: Missing Pollfish API Key");
            return;
        }

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "pollfishAPIKey(): " + pollfishAPIKey);

        mMediationAdLoadCallback = mediationAdLoadCallback;

        final ViewGroup viewGroup =(ViewGroup) ((Activity) getContext()).getWindow().getDecorView().getRootView();

        PollFish.initWith((Activity) getContext(), new PollFish.ParamsBuilder(pollfishAPIKey)
                .rewardMode(false)
                .releaseMode(releaseMode)
                .requestUUID(requestUUID)

                /**
                 * Piollfish callbacks for AdMob Mediation.
                 */

                .pollfishReceivedSurveyListener(new PollfishReceivedSurveyListener() {
                    @Override
                    public void onPollfishSurveyReceived(SurveyInfo surveyInfo) {

                        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "onPollfishSurveyReceived");

                        if (mMediationAdLoadCallback != null) {
                            mMediationRewardedAdCallback = mMediationAdLoadCallback
                                    .onSuccess(PollfishAdMobAdapter.this);
                        }
                    }
                })
                .pollfishUserRejectedSurveyListener(new PollfishUserRejectedSurveyListener() {
                    @Override
                    public void onUserRejectedSurvey() {
                        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "onUserRejectedSurvey");

                        if (mMediationAdLoadCallback != null) {
                            mMediationRewardedAdCallback.onAdFailedToShow("onUserRejectedSurvey");
                        }
                    }
                })
                .pollfishSurveyNotAvailableListener(new PollfishSurveyNotAvailableListener() {
                    @Override
                    public void onPollfishSurveyNotAvailable() {

                        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "onPollfishSurveyNotAvailable");

                        if (mMediationAdLoadCallback != null) {
                            mMediationAdLoadCallback.onFailure("Pollfish Surveys Not Available");
                        }

                    }
                })
                .pollfishOpenedListener(
                        new PollfishOpenedListener() {
                            @Override
                            public void onPollfishOpened() {

                                if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "onPollfishOpened");

                                if (mMediationRewardedAdCallback != null) {
                                    mMediationRewardedAdCallback.onAdOpened();
                                    mMediationRewardedAdCallback.onVideoStart();
                                    mMediationRewardedAdCallback.reportAdImpression();
                                }
                            }
                        }
                )
                .pollfishClosedListener(
                        new PollfishClosedListener() {
                            @Override
                            public void onPollfishClosed() {

                                if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "onPollfishClosed");

                                postRunnableInMainThread(getContext(), new Runnable() {
                                            @Override
                                            public void run() {

                                                // We need to re-activate video/sound and other JS WebView functionality that were paused

                                                if(viewGroup!=null){
                                                    activateJSinView(viewGroup);
                                                }

                                                if (mMediationRewardedAdCallback != null) {
                                                    mMediationRewardedAdCallback.onAdClosed();
                                                }
                                            }
                                        }, 400
                                );
                            }
                        })

                .pollfishUserNotEligibleListener(new PollfishUserNotEligibleListener() {
                    @Override
                    public void onUserNotEligible() {

                        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "onUserNotEligible");

                        if (mMediationRewardedAdCallback != null) {
                            mMediationRewardedAdCallback.onAdFailedToShow("onUserNotEligible");
                        }

                    }
                })
                .pollfishCompletedSurveyListener(new PollfishCompletedSurveyListener() {
                    @Override
                    public void onPollfishSurveyCompleted(SurveyInfo surveyInfo) {

                        final PollfishReward reward = new PollfishReward(surveyInfo.getRewardName(), surveyInfo.getRewardValue());

                        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "onPollfishSurveyCompleted");

                        if (mMediationRewardedAdCallback != null) {
                            mMediationRewardedAdCallback.onVideoComplete();
                            mMediationRewardedAdCallback.onUserEarnedReward(reward);
                        }
                    }
                })
                .rewardMode(true)
                .build());
    }


    @Override
    public void showAd(Context context) {

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "showAd()");

        if (mMediationRewardedAdCallback != null) {
            mMediationRewardedAdCallback.reportAdClicked();
        }

        PollFish.show();
    }


    @Override
    public VersionInfo getVersionInfo() {

        String versionString = PollfishAdMobAdapterConstants.POLLFISH_ADAPTER_VERSION;

        String splits[] = versionString.split("\\.");

        if (splits.length >= 4) {

            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]) * 100 + Integer.parseInt(splits[3]);

            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "getVersionInfo():" + major + " " + minor + " " + micro);

            return new VersionInfo(major, minor, micro);
        }

        Log.d(TAG, String.format("Unexpected adapter version format: %s." +
                "Returning 0.0.0 for adapter version.", versionString));

        return new VersionInfo(0, 0, 0);
    }


    @Override
    public VersionInfo getSDKVersionInfo() {

        String versionString = PollfishAdMobAdapterConstants.POLLFISH_ADAPTER_VERSION;

        String splits[] = versionString.split("\\.");

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

    private  void postRunnableInMainThread(Context ctx, Runnable r,
                                                int delay) {
        try {
            Handler mainHandler = new Handler(ctx.getMainLooper());
            mainHandler.postDelayed(r, delay);
        } catch (Exception e) {

            if (PollfishAdMobAdapterConstants.DEBUGMODE)  Log.e(TAG, "postRunnableInMainThread:" + e);
        }
    }

    private void activateJSinView(ViewGroup parent) {

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "Activate back JS in WebView");

        for (int i = 0; i < parent.getChildCount(); i++) {

            View child = parent.getChildAt(i);

            if ((child!=null) && (child instanceof WebView)) {

                WebView childWebView = (WebView) child;

                if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "Found WebView - Activating JS back");

                childWebView.resumeTimers();
                childWebView.onResume();
            }

            if ((child!=null) && (child instanceof ViewGroup)) {

                activateJSinView((ViewGroup) child);
            }
        }
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
