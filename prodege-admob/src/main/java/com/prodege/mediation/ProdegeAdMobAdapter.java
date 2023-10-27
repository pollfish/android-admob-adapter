package com.prodege.mediation;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.VersionInfo;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.prodege.Prodege;
import com.prodege.builder.AdRequest;
import com.prodege.builder.InitOptions;
import com.prodege.builder.Platform;
import com.prodege.builder.SurveyFormat;
import com.prodege.listener.ProdegeEventListener;
import com.prodege.listener.ProdegeException;
import com.prodege.listener.ProdegeInitListener;
import com.prodege.listener.ProdegeRewardedInfo;
import com.prodege.listener.ProdegeRewardedLoadListener;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.List;

public class ProdegeAdMobAdapter extends Adapter {


    /**
     * Prodege adapter errors.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {
            ERROR_LOW_TARGET,
            ERROR_MISSING_SERVER_PARAMETERS,
            ERROR_INVALID_CONFIGURATION,
            ERROR_NOT_INITIALIZED,
            ERROR_INTERNAL,
            ERROR_NO_CONNECTION,
            ERROR_NO_FILL,
            ERROR_PLACEMENT_ALREADY_LOADED
    })
    public @interface AdapterError {

    }

    /**
     * Low Android target.
     */
    public static final int ERROR_LOW_TARGET = 101;

    /**
     * Invalid server parameters.
     */
    public static final int ERROR_MISSING_SERVER_PARAMETERS = 102;

    /**
     * Invalid configuration.
     */
    public static final int ERROR_INVALID_CONFIGURATION = 103;

    /**
     * Prodege SDK not initialized.
     */
    public static final int ERROR_NOT_INITIALIZED = 104;

    /**
     * Presentation error occurred during video playback.
     */
    public static final int ERROR_INTERNAL = 105;

    /**
     * Presentation error occurred during video playback.
     */
    public static final int ERROR_NO_CONNECTION = 106;

    /**
     * Prodege SDK has no content available.
     */
    public static final int ERROR_NO_FILL = 108;

    /**
     * Placement already loaded.
     */
    public static final int ERROR_PLACEMENT_ALREADY_LOADED = 109;
    // endregion

    @Nullable
    private static Boolean localTestMode = null;

    @Nullable
    private static String localUserId = null;

    @Nullable
    private static String localApiKey = null;

    public static void setTestMode(boolean testMode) {
        ProdegeAdMobAdapter.localTestMode = testMode;
    }

    public static void setUserId(@NotNull String userId) {
        ProdegeAdMobAdapter.localUserId = userId;
    }

    public static void setApiKey(@NotNull String apiKey) {
        ProdegeAdMobAdapter.localApiKey = apiKey;
    }

    @Override
    public void initialize(@NonNull Context context,
                           @NonNull InitializationCompleteCallback initializationCompleteCallback,
                           @NonNull List<MediationConfiguration> mediationConfigurations) {

        if (Build.VERSION.SDK_INT < 21) {
            AdError adError = new AdError(ERROR_LOW_TARGET,
                    "Prodege SDK will not run on targets lower than 21.", ProdegeConstants.DOMAIN);
            initializationCompleteCallback.onInitializationFailed(adError.toString());
            return;
        }

        HashSet<ProdegeInitializationConfiguration> configurations = new HashSet<>();

        for (MediationConfiguration configuration : mediationConfigurations) {
            ProdegeInitializationConfiguration adapterInitConfiguration =
                    ProdegeInitializationConfiguration.fromBundle(configuration.getServerParameters(), localTestMode, localUserId, localApiKey);

            if (adapterInitConfiguration != null) {
                configurations.add(adapterInitConfiguration);
            } else {
                AdError adError = new AdError(
                        ERROR_INVALID_CONFIGURATION,
                        "Invalid adapter configuration",
                        ProdegeConstants.DOMAIN);
                initializationCompleteCallback.onInitializationFailed(adError.toString());
                return;
            }
        }

        ProdegeInitializationConfiguration adapterConfiguration;
        int count = configurations.size();
        if (count == 0) {
            AdError error = new AdError(ERROR_MISSING_SERVER_PARAMETERS,
                    "Missing Prodege configuration.", ProdegeConstants.DOMAIN);
            initializationCompleteCallback.onInitializationFailed(error.toString());
            return;
        }

        adapterConfiguration = configurations.iterator().next();

        if (count > 1) {
            String logMessage = String
                    .format("Multiple configuration entries found: %s. Using '%s' to initialize the Prodege SDK.", configurations, adapterConfiguration);
            Log.w(ProdegeConstants.TAG, logMessage);
        }

        InitOptions.Builder initOptionsBuilder = new InitOptions.Builder()
                .platform(Platform.ADMOB);

        if (adapterConfiguration.userId != null) {
            initOptionsBuilder.userId(adapterConfiguration.userId);
        }

        if (adapterConfiguration.testMode != null) {
            initOptionsBuilder.testMode(adapterConfiguration.testMode);
        }

        InitOptions initOptions = initOptionsBuilder.build();

        Prodege.initialize(context, adapterConfiguration.apiKey, new ProdegeInitListener() {
            @Override
            public void onError(@NonNull ProdegeException e) {
                initializationCompleteCallback.onInitializationFailed(e.toString());
            }

            @Override
            public void onSuccess() {
                initializationCompleteCallback.onInitializationSucceeded();
            }
        }, initOptions);
    }

    @Override
    public void loadRewardedAd(@NonNull MediationRewardedAdConfiguration mediationRewardedAdConfiguration,
                               @NonNull MediationAdLoadCallback<MediationRewardedAd,
                                       MediationRewardedAdCallback> mediationAdLoadCallback) {
        if (Build.VERSION.SDK_INT < 21) {
            mediationAdLoadCallback.onFailure(
                    new AdError(ERROR_LOW_TARGET,
                            "Prodege SDK will not run on targets lower than 21.", ProdegeConstants.DOMAIN)
            );
            return;
        }

        ProdegeRequestConfiguration requestConfiguration = ProdegeRequestConfiguration.fromBundle(
                mediationRewardedAdConfiguration.getServerParameters(),
                mediationRewardedAdConfiguration.getMediationExtras()
        );

        if (requestConfiguration == null) {
            mediationAdLoadCallback.onFailure(
                    new AdError(
                            ERROR_INVALID_CONFIGURATION,
                            "Invalid adapter configuration.",
                            ProdegeConstants.DOMAIN));
            return;
        }

        if (Prodege.isPlacementVisible(requestConfiguration.placementId)) {
            mediationAdLoadCallback.onFailure(
                    new AdError(
                            ERROR_PLACEMENT_ALREADY_LOADED,
                            "Invalid adapter configuration.",
                            ProdegeConstants.DOMAIN));
            return;
        }

        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

        if (requestConfiguration.requestUuid != null) {
            adRequestBuilder.requestUuid(requestConfiguration.requestUuid);
        }

        if (requestConfiguration.surveyFormat != null && requestConfiguration.surveyFormat >= 0 && requestConfiguration.surveyFormat < SurveyFormat.values().length) {
            adRequestBuilder.surveyFormat(SurveyFormat.values()[requestConfiguration.surveyFormat]);
        }

        Prodege.loadRewardedAd(requestConfiguration.placementId, new ProdegeRewardedLoadListener() {
            @Override
            public void onRewardedLoaded(@NonNull String s, @NonNull ProdegeRewardedInfo prodegeRewardedInfo) {
                @NonNull ProdegeMediationRewardedAd rewardedAd = new ProdegeMediationRewardedAd(requestConfiguration);
                MediationRewardedAdCallback mMediationRewardedAdCallback = mediationAdLoadCallback.onSuccess(rewardedAd);
                rewardedAd.setMediationRewardedAdCallback(mMediationRewardedAdCallback);
                Prodege.setEventListener(new ProdegeEventListener() {
                    @Override
                    public void onStart(@NonNull String s) {
                        mMediationRewardedAdCallback.onVideoStart();
                    }

                    @Override
                    public void onComplete(@NonNull String s) {
                        mMediationRewardedAdCallback.onVideoComplete();
                    }

                    @Override
                    public void onUserReject(@NonNull String s) {
                    }

                    @Override
                    public void onUserNotEligible(@NonNull String s) {
                    }

                    @Override
                    public void onClick(@NonNull String e) {
                        mMediationRewardedAdCallback.reportAdClicked();
                    }
                });
                Prodege.setRewardListener(prodegeReward -> mMediationRewardedAdCallback.onUserEarnedReward(new com.prodege.mediation.ProdegeReward(prodegeReward.getCurrency(), prodegeReward.getPoints())));
            }

            @Override
            public void onRewardedLoadFailed(@NonNull String s, @NonNull ProdegeException e) {
                mediationAdLoadCallback.onFailure(ProdegeAdMobAdapterUtils.toAdError(e));
            }
        }, adRequestBuilder.build());
    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {

        String versionString = BuildConfig.ADAPTER_VERSION;

        String[] splits = versionString.split("\\.");

        if (splits.length >= 4) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]) * 100 + Integer.parseInt(splits[3]);

            return new VersionInfo(major, minor, micro);
        }

        Log.d(ProdegeConstants.TAG, String.format("Unexpected adapter version format: %s." +
                "Returning 0.0.0 for adapter version.", versionString));

        return new VersionInfo(0, 0, 0);
    }

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {

        String versionString = BuildConfig.PRODEGE_SDK_VERSION;

        String[] splits = versionString.split("\\.");

        if (splits.length >= 3) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]);

            return new VersionInfo(major, minor, micro);
        }

        Log.d(ProdegeConstants.TAG, String.format("Unexpected adapter version format: %s." +
                "Returning 0.0.0 for adapter version.", versionString));

        return new VersionInfo(0, 0, 0);
    }

}
