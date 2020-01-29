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

import org.json.JSONObject;

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
    private boolean releaseMode =true;

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

    private static boolean pollfishPanelOpen = false;


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
        mMediationAdLoadCallback = mediationAdLoadCallback;

        if(pollfishPanelOpen){

            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "Pollfish Survey Panel already visible");

            if (mMediationAdLoadCallback != null) {
                mMediationAdLoadCallback.onFailure("Pollfish Survey Panel already visible");
            }
            return;
        }

        if (!(getContext() instanceof Activity)) {
            mediationAdLoadCallback.onFailure("Context is not an Activity. Pollfish requires an Activity context to load surveys.");
            return;
        }

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "MediationRewardedAdConfiguration:" + mediationRewardedAdConfiguration.getServerParameters().toString());


        Bundle serverParameters = mediationRewardedAdConfiguration.getServerParameters();
        Bundle networkExtras = mediationRewardedAdConfiguration.getMediationExtras();

        if (networkExtras != null) {

            pollfishAPIKey = networkExtras.getString(PollfishExtrasBundleBuilder.POLLFISH_API_KEY);
            releaseMode = networkExtras.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_MODE, true);
            requestUUID = networkExtras.getString(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID);
            offerwallMode= networkExtras.getBoolean(PollfishExtrasBundleBuilder.POLLFISH_INTEGRATION_TYPE,false);

            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd() networkExtras key: " + pollfishAPIKey);
            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd() networkExtras mode: " + releaseMode);
            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd() networkExtras requestUUID: " + requestUUID);
            if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "loadRewardedAd() networkExtras offerwallMode: " + offerwallMode);
        }

        if (serverParameters != null) {

            try {
                String jsonParams = serverParameters.getString("parameter");

                try {

                    JSONObject jsonObject = new JSONObject(jsonParams);

                    if(jsonObject!=null) {

                        Log.d(TAG, "Pollfish jsonParams: " + jsonObject.toString());

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_API_KEY)) {
                            //Checking address Key Present or not
                            pollfishAPIKey = jsonObject .getString(PollfishExtrasBundleBuilder.POLLFISH_API_KEY); // Present Key

                            if (PollfishAdMobAdapterConstants.DEBUGMODE)
                                Log.d(TAG, "Pollfish API Key from AdMob UI: " + pollfishAPIKey);
                        }

                        if (jsonObject.has(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID)) {

                            requestUUID = jsonObject .getString(PollfishExtrasBundleBuilder.POLLFISH_REQUEST_UUID); // Present Key

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
                    Log.e(TAG,"Could not parse malformed JSON: " + jsonParams);
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

        pollfishPanelOpen = false;

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

                        if (mMediationRewardedAdCallback != null) {
                            mMediationRewardedAdCallback.onAdClosed();
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

                        pollfishPanelOpen = false;

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

                                pollfishPanelOpen = true;
                            }
                        }
                )
                .pollfishClosedListener(
                        new PollfishClosedListener() {
                            @Override
                            public void onPollfishClosed() {

                                if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "onPollfishClosed");

                                if (mMediationRewardedAdCallback != null) {
                                    mMediationRewardedAdCallback.onAdClosed();
                                }

                                pollfishPanelOpen = false;
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
                .offerWallMode(offerwallMode)
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
